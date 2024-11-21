package it.gov.pagopa.rtp.activator.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import it.gov.pagopa.rtp.activator.domain.Payer;
import it.gov.pagopa.rtp.activator.domain.PayerRepository;

@Repository
public class ActivationDBRepository implements PayerRepository {

  private final ActivationDB activationDB;
  private final ActivationMapper activationMapper;

  public ActivationDBRepository(ActivationDB activationDB,
      ActivationMapper activationMapper) {
    this.activationDB = activationDB;
    this.activationMapper = activationMapper;
  }

  @Override
  public Optional<Payer> findByFiscalCode(String fiscalCode) {
    return activationDB.findByFiscalCode(fiscalCode).map(activationMapper::toDomain);
  }

  @Override
  public void save(Payer payer) {
    activationDB.save(activationMapper.toDbEntity(payer));
  }

}
