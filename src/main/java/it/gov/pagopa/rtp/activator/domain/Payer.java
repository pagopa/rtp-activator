package it.gov.pagopa.rtp.activator.domain;

import java.time.Instant;

public record Payer(PayerID payerID, String rtpSpId, String fiscalCode, Instant effectiveActivationDate)  {
}
