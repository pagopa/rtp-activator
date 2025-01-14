package it.gov.pagopa.rtp.activator.domain.rtp;

import java.util.UUID;
import lombok.Getter;

@Getter
public class ResourceID {

  private final UUID id;

  public ResourceID(UUID uuid) {
    this.id = uuid;
  }

  public static ResourceID createNew() {
    UUID uuid = UUID.randomUUID();
    return new ResourceID(uuid);
  }
}
