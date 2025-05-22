package it.gov.pagopa.rtp.activator.repository.activation;

import it.gov.pagopa.rtp.activator.telemetry.TraceMongo;
import java.util.UUID;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository("deletedActivationDB")
@TraceMongo
public interface DeletedActivationDB extends ReactiveMongoRepository<DeletedActivationEntity, UUID> {

}
