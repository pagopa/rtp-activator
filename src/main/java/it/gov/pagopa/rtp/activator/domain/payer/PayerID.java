package it.gov.pagopa.rtp.activator.domain.payer;

import java.util.UUID;

import lombok.Getter;

@Getter
public class PayerID {

    private final UUID id;

    public PayerID(UUID uuid) {
        this.id = uuid;
    }

    public static PayerID createNew() {
        UUID uuid = UUID.randomUUID();
        return new PayerID(uuid);
    }

}
