package it.gov.pagopa.rtp.activator.service.callback;

import reactor.core.publisher.Mono;

public interface RequestToPayUpdateService {
    public Mono<String> checkCallback(String certificateSerialNumber, String serviceProviderDebtorId);
}
