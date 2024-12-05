package it.gov.pagopa.rtp.activator.domain.rtp;

import java.util.UUID;

import lombok.Getter;

@Getter
public class RequestID {

    private final UUID id;

    public RequestID(UUID uuid) {
        this.id = uuid;
    }

    public static RequestID createNew() {
        UUID uuid = UUID.randomUUID();
        return new RequestID(uuid);
    }

}
