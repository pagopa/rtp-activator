package it.gov.pagopa.rtp.activator.service;

import it.gov.pagopa.rtp.activator.model.generated.ActivationDto;

public interface ActivationPayerService {
    ActivationDto activatePayer(String payer, String fiscalCode);
}