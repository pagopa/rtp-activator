package it.gov.pagopa.rtp.activator.service.rtp;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.model.ActivationDto;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.domain.errors.MessageBadFormed;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotActivatedException;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.epcClient.model.ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalServiceLevel1CodeWrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.IBAN2007IdentifierWrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ISODateWrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.Max35TextWrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29EPC25922V30DS022WrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.PersonIdentification13EPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.epcClient.model.SepaRequestToPayRequestResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.SynchronousSepaRequestToPayCreationResponseDto;
import it.gov.pagopa.rtp.activator.service.rtp.handler.SendRtpProcessor;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Service
@Slf4j
@RegisterReflectionForBinding({SepaRequestToPayRequestResourceDto.class,
    PersonIdentification13EPC25922V30DS02WrapperDto.class, ISODateWrapperDto.class,
    ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto.class,
    ExternalServiceLevel1CodeWrapperDto.class,
    ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto.class, Max35TextWrapperDto.class,
    OrganisationIdentification29EPC25922V30DS022WrapperDto.class,
    ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto.class,
    IBAN2007IdentifierWrapperDto.class,
    ActivationDto.class,
    SynchronousSepaRequestToPayCreationResponseDto.class
})
public class SendRTPServiceImpl implements SendRTPService {

  private final SepaRequestToPayMapper sepaRequestToPayMapper;
  private final ReadApi activationApi;
  private final ObjectMapper objectMapper;
  private final ServiceProviderConfig serviceProviderConfig;
  private final RtpRepository rtpRepository;
  private final SendRtpProcessor sendRtpProcessor;

  public SendRTPServiceImpl(SepaRequestToPayMapper sepaRequestToPayMapper, ReadApi activationApi,
      ServiceProviderConfig serviceProviderConfig, RtpRepository rtpRepository, SendRtpProcessor sendRtpProcessor) {
    this.sepaRequestToPayMapper = sepaRequestToPayMapper;
    this.activationApi = activationApi;
    this.serviceProviderConfig = serviceProviderConfig;
    this.rtpRepository = rtpRepository;
    this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    this.sendRtpProcessor = sendRtpProcessor;
  }

  @NonNull
  @Override
  public Mono<Rtp> send(@NonNull final Rtp rtp) {
    Objects.requireNonNull(rtp, "Rtp cannot be null");

    final var activationData = activationApi.findActivationByPayerId(UUID.randomUUID(),
            rtp.payerId(),
            serviceProviderConfig.activation().apiVersion())
        .doFirst(() -> log.info("Finding activation data for payerId: {}", rtp.payerId()))
        .doOnSuccess(act -> log.info("Activation data found for payerId: {}", rtp.payerId()))
        .doOnError(error -> log.error("Error finding activation data for payerId: {}", rtp.payerId(), error))
        .onErrorMap(WebClientResponseException.class, this::mapActivationResponseToException);

    final var rtpToSend = activationData.map(act -> act.getPayer().getRtpSpId())
        .map(rtp::toRtpWithActivationInfo)
        .doOnSuccess(rtpWithActivationInfo -> log.info("Saving Rtp to be sent: {}", rtpWithActivationInfo))
        .flatMap(rtpRepository::save)
        .flatMap(this::logRtpAsJson)
        .doOnSuccess(rtpSaved -> log.info("Rtp to be sent saved with id: {}", rtpSaved.resourceID().getId()))
        .doOnError(error -> log.error("Error saving Rtp to be sent: {}", error.getMessage(), error));

    final var sentRtp = rtpToSend.flatMap(this.sendRtpProcessor::sendRtpToServiceProviderDebtor)
        .map(rtp::toRtpSent)
        .flatMap(
            rtpToSave -> rtpRepository.save(rtpToSave)
                .retryWhen(sendRetryPolicy())
                .doOnError(ex -> log.error("Failed after retries", ex))

        );

    return sentRtp.doOnSuccess(
            rtpSaved -> log.info("RTP saved with id: {}", rtpSaved.resourceID().getId()))
        .doOnError(error -> log.error("Error sending RTP: {}", error.getMessage(), error))
        .onErrorMap(WebClientResponseException.class, this::mapExternalSendResponseToException)
        .switchIfEmpty(Mono.error(new PayerNotActivatedException()));
  }


  @NonNull
  @Override
  public Mono<Rtp> cancelRtp(@NonNull final ResourceID rtpId) {
    return this.rtpRepository.findById(rtpId)
        .doFirst(() -> log.info("Retrieving RTP with id {}", rtpId.getId()))
        .doOnSuccess(rtp -> log.info("RTP retrieved with id {} and status {}", rtp.resourceID().getId(), rtp.status()))
        .doOnError(error -> log.error("Error retrieving RTP: {}", error.getMessage(), error))
        .filter(rtp -> rtp.status().equals(RtpStatus.CREATED))
        .switchIfEmpty(Mono.error(() -> new IllegalStateException("Cannot cancel RTP with id " + rtpId.getId())))
        .flatMap(this.sendRtpProcessor::sendRtpCancellationToServiceProviderDebtor)
        .doOnNext(rtp -> log.debug("Setting status of RTP with id {} to {}", rtp.resourceID().getId(), RtpStatus.CANCELLED))
        .map(rtp -> rtp.withStatus(RtpStatus.CANCELLED))
        .doOnNext(rtp -> log.info("Saving {} RTP with id {}", rtp.status(), rtp.resourceID().getId()))
        .flatMap(this.rtpRepository::save);
  }


  private Throwable mapActivationResponseToException(WebClientResponseException exception) {
    return switch (exception.getStatusCode()) {
      case NOT_FOUND -> new PayerNotActivatedException();
      case BAD_REQUEST -> new MessageBadFormed(exception.getResponseBodyAsString());
      default -> new RuntimeException("Internal Server Error");
    };
  }

  private Mono<Rtp> logRtpAsJson(Rtp rtp) {
    log.info(rtpToJson(rtp));
    return Mono.just(rtp);
  }

  private String rtpToJson(Rtp rtpToLog) {
    try {
      return objectMapper.writeValueAsString(
          sepaRequestToPayMapper.toEpcRequestToPay(rtpToLog));
    } catch (JsonProcessingException e) {
      log.error("Problem while serializing SepaRequestToPayRequestResourceDto object", e);
      return "";
    }
  }

  private RetryBackoffSpec sendRetryPolicy() {
    final var maxAttempts = serviceProviderConfig.send().retry().maxAttempts();
    final var minDurationMillis = serviceProviderConfig.send().retry().backoffMinDuration();
    final var jitter = serviceProviderConfig.send().retry().backoffJitter();

    return Retry.backoff(maxAttempts, Duration.ofMillis(minDurationMillis))
        .jitter(jitter)
        .doAfterRetry(signal -> log.info("Retry number {}", signal.totalRetries()));
  }

  private Throwable mapExternalSendResponseToException(WebClientResponseException exception) {
    return new UnsupportedOperationException("Unsupported exception handling for epc response");
  }

}