package it.gov.pagopa.rtp.activator.repository.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RtpDBRepository implements RtpRepository {

  private final RtpDB rtpDB;
  private final RtpMapper rtpMapper;

  @Override
  public Mono<Rtp> save(Rtp rtp) {
    log.info("Saving RTP {} in state {}", rtp.resourceID().getId(), rtp.status());
    return rtpDB.save(rtpMapper.toDbEntity(rtp))
        .map(rtpMapper::toDomain);
  }


  @NonNull
  @Override
  public Mono<Rtp> findById(@NonNull final ResourceID resourceID) {
    return Mono.just(resourceID)
        .doFirst(() -> log.debug("Retrieving RTP with id {}", resourceID.getId()))
        .map(ResourceID::getId)
        .flatMap(rtpDB::findById)
        .map(rtpMapper::toDomain);
  }
}
