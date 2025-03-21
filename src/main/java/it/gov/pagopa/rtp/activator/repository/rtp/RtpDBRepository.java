package it.gov.pagopa.rtp.activator.repository.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
