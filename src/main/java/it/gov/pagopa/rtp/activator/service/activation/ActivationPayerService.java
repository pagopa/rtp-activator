package it.gov.pagopa.rtp.activator.service.activation;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import java.util.UUID;
import reactor.core.publisher.Mono;

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
}
