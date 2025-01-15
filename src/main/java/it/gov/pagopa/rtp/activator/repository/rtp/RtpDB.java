package it.gov.pagopa.rtp.activator.repository.rtp;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RtpDB extends ReactiveMongoRepository<RtpEntity, String> {

}
