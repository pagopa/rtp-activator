package it.gov.pagopa.rtp.activator.domain;

import java.util.Date;

public record Payer(PayerID payerID, String rtpSpId, String fiscalCode, Date effectiveActivationDate)  {
}
