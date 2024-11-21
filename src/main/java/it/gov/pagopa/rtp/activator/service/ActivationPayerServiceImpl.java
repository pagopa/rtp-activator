package it.gov.pagopa.rtp.activator.service;

import it.gov.pagopa.rtp.activator.model.generated.ActivationDto;

public class ActivationPayerServiceImpl implements ActivationPayerService{

    @Override
    public ActivationDto activatePayer(String payer, String fiscalCode) {
        // TODO Auto-generated method stub
        // Try to save params into db
        // check the db response
        // if it's ok response 200
        // if the record already exists
        // report 409 error
        throw new UnsupportedOperationException("Unimplemented method 'activatePayer'");
    }


}
