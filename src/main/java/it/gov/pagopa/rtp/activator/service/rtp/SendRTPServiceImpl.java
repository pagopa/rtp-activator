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
import it.gov.pagopa.rtp.activator.service.oauth.Oauth2TokenService;
import it.gov.pagopa.rtp.activator.service.registryfile.RegistryDataService;
import it.gov.pagopa.rtp.activator.utils.ExceptionUtils;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Service
@Slf4j
@RegisterReflectionForBinding({ SepaRequestToPayRequestResourceDto.class,
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
  private final RegistryDataService registryDataService;

  private final Oauth2TokenService oauth2TokenService;
  private final Environment environment;

  public SendRTPServiceImpl(SepaRequestToPayMapper sepaRequestToPayMapper, ReadApi activationApi,
      ServiceProviderConfig serviceProviderConfig, RtpRepository rtpRepository,
      DefaultApi sendApi, RegistryDataService registryDataService,
      Oauth2TokenService oauth2TokenService, Environment environment) {
    this.sepaRequestToPayMapper = sepaRequestToPayMapper;
    this.activationApi = activationApi;
    this.serviceProviderConfig = serviceProviderConfig;
    this.rtpRepository = rtpRepository;
    this.sendApi = sendApi;
    this.registryDataService = registryDataService;
    this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    this.oauth2TokenService = oauth2TokenService;
    this.environment = environment;
  }

  @NonNull
  @Override
  public Mono<Rtp> send(@NonNull final Rtp rtp) {
    Objects.requireNonNull(rtp, "Rtp cannot be null");

    final var activationData = activationApi.findActivationByPayerId(UUID.randomUUID(),
        rtp.payerId(),
        serviceProviderConfig.activation().apiVersion())
        .doOnSuccess(act -> log.info("Activation data found for payerId: {}", rtp.payerId()))
        .onErrorMap(WebClientResponseException.class, this::mapActivationResponseToException);

    final var rtpToSend = activationData.map(act -> act.getPayer().getRtpSpId())
        .map(rtp::toRtpWithActivationInfo)
        .flatMap(rtpRepository::save)
        .flatMap(this::logRtpAsJson)
        .doOnSuccess(rtpSaved -> log.info("Rtp to be sent saved with id: {}", rtpSaved.resourceID().getId()))
        .doOnError(error -> log.error("Error saving Rtp to be sent: {}", error.getMessage(), error));

    final var sentRtp = rtpToSend.flatMap(this::sendRtpToServiceProviderDebtor)
        .map(rtp::toRtpSent)
        .flatMap(
            rtpToSave -> rtpRepository.save(rtpToSave)
                .retryWhen(sendRetryPolicy())
                .doOnError(ex -> log.error("Failed after retries", ex))

        );

    return sentRtp.doOnSuccess(
        rtpSaved -> log.info("RTP saved with id: {}", rtpSaved.resourceID().getId()))
        .onErrorMap(WebClientResponseException.class, this::mapExternalSendResponseToException)
        .switchIfEmpty(Mono.error(new PayerNotActivatedException()));
  }

  @NonNull
  private Mono<Rtp> sendRtpToServiceProviderDebtor(@NonNull final Rtp rtpToSend) {
    Objects.requireNonNull(rtpToSend, "Rtp to send cannot be null.");

    return registryDataService.getRegistryData()
        .map(data -> data.get(rtpToSend.serviceProviderDebtor()))
        .switchIfEmpty(Mono.error(new IllegalStateException(
            "No service provider found for creditor: " + rtpToSend.serviceProviderDebtor())))
        .flatMap(provider -> {

          String basePath = provider.tsp().serviceEndpoint();

          String tokenEndpoint = provider.tsp().oauth2().tokenEndpoint();
          String clientId = provider.tsp().oauth2().clientId();
          String clientSecret = environment.getProperty("client." + provider.tsp().oauth2().clientSecretEnvVar());
          String scope = provider.tsp().oauth2().scope();

          sendApi.getApiClient().setBasePath(basePath);

          return oauth2TokenService
              .getAccessToken(tokenEndpoint, clientId, clientSecret, scope)
              .flatMap(token -> {
                sendApi.getApiClient().addDefaultHeader("Authorization", "Bearer " + token);
                return sendApi.postRequestToPayRequests(
                    UUID.randomUUID(),
                    UUID.randomUUID().toString(),
                    sepaRequestToPayMapper.toEpcRequestToPay(rtpToSend));
                // .retryWhen(sendRetryPolicy()); // disable until we'll not have a 200 response
              }).onErrorMap(ExceptionUtils::gracefullyHandleError).map(response -> rtpToSend).defaultIfEmpty(rtpToSend)
              .doOnSuccess(rtpSent -> log.info("RTP sent to {} with id: {}",
                  rtpSent.serviceProviderDebtor(), rtpSent.resourceID().getId()));
        });
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