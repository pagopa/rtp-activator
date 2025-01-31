package it.gov.pagopa.rtp.activator.domain.payer;

import java.time.Instant;

public record Payer(ActivationID activationID, String serviceProviderDebtor, String fiscalCode, Instant effectiveActivationDate)  {
}
