package it.gov.pagopa.rtp.activator.repository.rtp;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import it.gov.pagopa.rtp.activator.telemetry.TraceMongo;

import java.util.UUID;

@Repository
@TraceMongo
public interface RtpDB extends ReactiveMongoRepository<RtpEntity, UUID> {

}
