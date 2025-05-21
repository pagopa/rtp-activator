package it.gov.pagopa.rtp.activator.controller.activation;

import static it.gov.pagopa.rtp.activator.utils.Authorizations.verifySubjectRequest;

import it.gov.pagopa.rtp.activator.configuration.ActivationPropertiesConfig;
import it.gov.pagopa.rtp.activator.controller.generated.activate.CreateApi;
import it.gov.pagopa.rtp.activator.controller.generated.activate.DeleteApi;
import it.gov.pagopa.rtp.activator.controller.generated.activate.ReadApi;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationReqDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageOfActivationsDto;
import it.gov.pagopa.rtp.activator.service.activation.ActivationPayerService;


import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * REST controller for handling RTP payer activations.
 * <p>
 * Implements the {@link CreateApi}, {@link ReadApi}, and {@link DeleteApi} interfaces to expose endpoints
 * for creating, reading, and deleting activation data.
 * </p>
 */
@RestController
@Validated
@Slf4j
public class ActivationAPIControllerImpl implements CreateApi, ReadApi, DeleteApi {

  private final ActivationPayerService activationPayerService;
  private final ActivationPropertiesConfig activationPropertiesConfig;
  private final ActivationDtoMapper activationDtoMapper;


  /**
   * Constructs a new {@code ActivationAPIControllerImpl}.
   *
   * @param activationPayerService the service handling activation logic
   * @param activationPropertiesConfig the configuration providing base URLs
   * @param activationDtoMapper the mapper for converting between DTOs and domain objects
   */
  public ActivationAPIControllerImpl(
      ActivationPayerService activationPayerService,
      ActivationPropertiesConfig activationPropertiesConfig,
      ActivationDtoMapper activationDtoMapper) {
    this.activationPayerService = activationPayerService;
    this.activationPropertiesConfig = activationPropertiesConfig;
    this.activationDtoMapper = activationDtoMapper;
  }


  /**
   * Activates a new RTP payer using the provided activation request DTO.
   *
   * @param requestId the unique ID of the request
   * @param version the API version
   * @param activationReqDto the request payload containing payer information
   * @param exchange the {@link ServerWebExchange} context
   * @return a {@link Mono} emitting a 201 Created response or an error
   */
  @Override
  @PreAuthorize("hasRole('write_rtp_activations')")
  public Mono<ResponseEntity<Void>> activate(
      UUID requestId,
      String version,
      Mono<ActivationReqDto> activationReqDto,
      ServerWebExchange exchange) {
    log.info("Received request to activate a payer");
    return verifySubjectRequest(activationReqDto, it -> it.getPayer().getRtpSpId())
        .flatMap(t -> activationPayerService.activatePayer(
            t.getPayer().getRtpSpId(),
            t.getPayer().getFiscalCode()))
        .<ResponseEntity<Void>>map(payer -> ResponseEntity
            .created(URI.create(activationPropertiesConfig.baseUrl()
                + payer.activationID().getId().toString()))
            .build())
        .doOnError(a -> log.error("Error activating payer {}", a.getMessage()));
  }


  /**
   * Retrieves an activation by payer fiscal code.
   *
   * @param requestId the request ID
   * @param payerId the fiscal code of the payer
   * @param version the API version
   * @param exchange the exchange context
   * @return a {@link Mono} emitting the activation DTO or 404 if not found
   */
  @Override
  @PreAuthorize("hasAnyRole('write_rtp_send','read_rtp_activations')")
  public Mono<ResponseEntity<ActivationDto>> findActivationByPayerId(
      UUID requestId,
      String payerId,
      String version,
      ServerWebExchange exchange) {
    log.info("Received request to find activation by payer id");
    return Mono.just(payerId)
        .flatMap(activationPayerService::findPayer)
        .map(activationDtoMapper::toActivationDto)
        .map(ResponseEntity::ok)
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }


  /**
   * Retrieves a single activation by its activation ID.
   * <p>Currently not implemented.</p>
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  @PreAuthorize("hasRole('read_rtp_activations')")
  public Mono<ResponseEntity<ActivationDto>> getActivation(
      UUID requestId, UUID activationId,
      String version, ServerWebExchange exchange) {
    throw new UnsupportedOperationException("Unimplemented method 'getActivation'");
  }


  /**
   * Retrieves a paginated list of activations.
   * <p>Currently not implemented.</p>
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  @PreAuthorize("hasRole('read_rtp_activations')")
  public Mono<ResponseEntity<PageOfActivationsDto>> getActivations(
      UUID requestId, Integer page, Integer size,
      String version, ServerWebExchange exchange) {
    throw new UnsupportedOperationException("Unimplemented method 'getActivations'");
  }


  /**
   * Deletes (deactivates) a payer by activation ID.
   *
   * @param requestId the request ID
   * @param activationId the ID of the activation to delete
   * @param version the API version
   * @param exchange the exchange context
   * @return a {@link Mono} emitting 204 No Content if deactivation is successful, or 404 if not found or unauthorized
   */
  @Override
  @PreAuthorize("hasAnyRole('write_rtp_activations')")
  public Mono<ResponseEntity<Void>> deleteActivation(UUID requestId, UUID activationId,
      String version, ServerWebExchange exchange) {

    return Mono.just(activationId)
        .doFirst(() -> log.info("Received request to deactivate payer. Id: {}", activationId))
        .flatMap(activationPayerService::findPayerById)

        .doOnNext(payer -> MDC.put("service_provider", payer.serviceProviderDebtor()))
        .doOnNext(payer -> MDC.put("debtor", payer.fiscalCode()))
        .doOnNext(payer -> MDC.put("activation_id", payer.activationID().getId().toString()))

        .doOnNext(payer -> log.info("Verifying token subject"))
        .flatMap(payer -> verifySubjectRequest(Mono.just(payer),
            payerToVerify -> Optional.of(payerToVerify)
                .map(Payer::serviceProviderDebtor)
                .orElse("")))

        .doOnNext(payer -> log.info("Deactivating payer"))
        .flatMap(activationPayerService::deactivatePayer)

        .doOnNext(deactivatedPayer -> log.info("Payer deactivated"))
        .map(deactivatedPayer -> ResponseEntity.noContent().<Void>build())

        .doOnError(ex -> log.error("Error deactivating payer {}", ex.getMessage(), ex))
        .onErrorReturn(AccessDeniedException.class,
            ResponseEntity.notFound().build())

        .switchIfEmpty(Mono.fromSupplier(() -> {
          log.error("Payer not found");
          return ResponseEntity.notFound().build();
        }))

        .doFinally(f -> MDC.clear());
  }
}
