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
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.model.generated.epc.ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalServiceLevel1CodeWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.IBAN2007IdentifierWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ISODateWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.Max35TextWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.OrganisationIdentification29EPC25922V30DS022WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PersonIdentification13EPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
    ActivationDto.class
})
public class SendRTPServiceImpl implements SendRTPService {

  private final SepaRequestToPayMapper sepaRequestToPayMapper;
  private final ReadApi activationApi;
  private final ObjectMapper objectMapper;
  private final ServiceProviderConfig serviceProviderConfig;
  private final RtpRepository rtpRepository;
  private final DefaultApi sendApi;

  public SendRTPServiceImpl(SepaRequestToPayMapper sepaRequestToPayMapper, ReadApi activationApi,
      ServiceProviderConfig serviceProviderConfig, RtpRepository rtpRepository,
      DefaultApi sendApi) {
    this.sepaRequestToPayMapper = sepaRequestToPayMapper;
    this.activationApi = activationApi;
    this.serviceProviderConfig = serviceProviderConfig;
    this.rtpRepository = rtpRepository;
    this.sendApi = sendApi;
    this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public Mono<Rtp> send(Rtp rtp) {

    return activationApi.findActivationByPayerId(UUID.randomUUID(), rtp.payerId(),
            serviceProviderConfig.apiVersion())
        .onErrorMap(WebClientResponseException.class, this::mapActivationResponseToException)
        .map(act -> act.getPayer().getRtpSpId())
        .map(rtp::toRtpWithActivationInfo)
        .flatMap(rtpRepository::save)
        .flatMap(this::logRtpAsJson)
        .flatMap(rtpToSend ->
            // response is ignored atm
            sendApi.postRequestToPayRequests(UUID.randomUUID(), UUID.randomUUID().toString(),
                    sepaRequestToPayMapper.toEpcRequestToPay(rtpToSend))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).jitter(0.75)
                    .doAfterRetry(signal -> log.info("Retry number {}", signal.totalRetries())))
                .onErrorMap(Throwable::getCause)
                .map(response -> rtpToSend)
                .defaultIfEmpty(rtpToSend)
        )
        .map(rtp::toRtpSent)
        .flatMap(rtpRepository::save)
        .doOnSuccess(rtpSaved -> log.info("RTP saved with id: {}", rtpSaved.resourceID().getId()))
        .onErrorMap(WebClientResponseException.class, this::mapExternalSendResponseToException)
        .switchIfEmpty(Mono.error(new PayerNotActivatedException()));
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

  private Throwable mapActivationResponseToException(WebClientResponseException exception) {
    return switch (exception.getStatusCode()) {
      case NOT_FOUND -> new PayerNotActivatedException();
      case BAD_REQUEST -> new MessageBadFormed(exception.getResponseBodyAsString());
      default -> new RuntimeException("Internal Server Error");
    };
  }

  private Throwable mapExternalSendResponseToException(WebClientResponseException exception) {
    return new UnsupportedOperationException("Unsupported exception handling for epc response");
  }

}