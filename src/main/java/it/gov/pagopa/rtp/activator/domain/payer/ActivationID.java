package it.gov.pagopa.rtp.activator.domain.payer;

import java.util.UUID;

import lombok.Getter;

@Getter
public class ActivationID {

    private final UUID id;

    public ActivationID(UUID uuid) {
        this.id = uuid;
    }

    public static ActivationID createNew() {
        UUID uuid = UUID.randomUUID();
        return new ActivationID(uuid);
    }

}
