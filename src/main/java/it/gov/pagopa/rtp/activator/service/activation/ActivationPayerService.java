package it.gov.pagopa.rtp.activator.service.activation;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationEntity;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Service interface for managing payer activations, including creating, retrieving, and deactivating payers.
 */
public interface ActivationPayerService {

   /**
    * Activates a new payer with the provided service provider debtor and fiscal code.
    *
    * @param payer the service provider debtor identifier
    * @param fiscalCode the fiscal code of the payer to activate
    * @return a {@link Mono} emitting the newly created {@link Payer}, or an error if the payer already exists
    */
   Mono<Payer> activatePayer(String payer, String fiscalCode);

   /**
    * Finds an active payer by their unique activation ID.
    *
    * @param id the UUID of the payer's activation
    * @return a {@link Mono} emitting the {@link Payer} if found, or empty if not
    */
   Mono<Payer> findPayerById(UUID id);

   /**
    * Finds an active payer by their fiscal code.
    *
    * @param payer the fiscal code of the payer
    * @return a {@link Mono} emitting the {@link Payer} if found, or empty if not
    */
   Mono<Payer> findPayer(String payer);

   /**
    * Deactivates the given payer.
    *
    * @param payer the {@link Payer} to deactivate
    * @return a {@link Mono} emitting the same {@link Payer} after deactivation
    */
   Mono<Payer> deactivatePayer(Payer payer);

   /**
    * Retrieves a paginated list of activation entities for a given service provider.
    *
    * @param serviceProvider the service provider
    * @param page the page number to retrieve (zero-based)
    * @param size the number of elements per page
    * @return a {@link Mono} emitting a {@link Tuple2} containing the list of {@link ActivationEntity} for the requested page
    *         and the total number of matching elements
    */
   Mono<Tuple2<List<ActivationEntity>, Long>> getActivationsByServiceProvider(String serviceProvider,
       int page, int size);
}
