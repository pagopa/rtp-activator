package it.gov.pagopa.rtp.activator.repository.rtp;


import java.util.UUID;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import it.gov.pagopa.rtp.activator.telemetry.TraceMongo;

@Repository
@TraceMongo
public interface RtpDB extends ReactiveMongoRepository<RtpEntity, UUID> {

}
