package it.gov.pagopa.rtp.activator.repository.activation;

import java.time.Instant;
import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;

/**
 * Mapper component responsible for converting between domain models and database entity representations
 * related to payer activations and deactivations.
 */
@Component
public class ActivationMapper {

    /**
     * Converts an {@link ActivationEntity} to its corresponding domain model {@link Payer}.
     *
     * @param activationEntity the database entity representing an active payer
     * @return the corresponding domain model {@link Payer}
     */
    public Payer toDomain(ActivationEntity activationEntity) {
        ActivationID activationID = new ActivationID((activationEntity.getId()));
        return new Payer(
            activationID,
            activationEntity.getServiceProviderDebtor(),
            activationEntity.getFiscalCode(),
            activationEntity.getEffectiveActivationDate()
        );
    }

    /**
     * Converts a domain model {@link Payer} to its corresponding database entity {@link ActivationEntity}.
     *
     * @param payer the domain model representing an active payer
     * @return the corresponding database entity {@link ActivationEntity}
     */
    public ActivationEntity toDbEntity(Payer payer) {
        return ActivationEntity.builder()
            .id(payer.activationID().getId())
            .fiscalCode(payer.fiscalCode())
            .serviceProviderDebtor(payer.serviceProviderDebtor())
            .effectiveActivationDate(payer.effectiveActivationDate())
            .build();
    }

    /**
     * Converts a domain model {@link Payer} to a {@link DeletedActivationEntity} to represent
     * a deactivated payer in the database.
     *
     * @param payer the domain model of the payer being deactivated
     * @return the corresponding {@link DeletedActivationEntity}
     */
    public DeletedActivationEntity toDeletedDbEntity(Payer payer) {
        return DeletedActivationEntity.builder()
            .id(payer.activationID().getId())
            .fiscalCode(payer.fiscalCode())
            .serviceProviderDebtor(payer.serviceProviderDebtor())
            .deactivationDate(Instant.now())
            .build();
    }
}

