package it.gov.pagopa.rtp.activator.service.oauth;

import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;

public interface MtlsApiClientFactory {
    public ApiClient createMtlsApiClient(String basePath);
}
