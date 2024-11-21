package it.gov.pagopa.rtp.activator.domain;

import java.util.Optional;

public interface PayerRepository {
    
    // Used to check if a specific payer is already registered.
    Optional<Payer> findByFiscalCode(String fiscalCode);

    void save(Payer payer);
    
}