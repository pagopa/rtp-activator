package it.gov.pagopa.rtp.activator.service.oauth;

import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.MtlsWebClientFactory;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;

@Component
public class MtlsApiClientFactoryDefault implements MtlsApiClientFactory {

  private final MtlsWebClientFactory mtlsWebClientFactory;

  public MtlsApiClientFactoryDefault(MtlsWebClientFactory mtlsWebClientFactory) {
    this.mtlsWebClientFactory = mtlsWebClientFactory;
  }

  @Override
  public ApiClient createMtlsApiClient(String basePath) {

    ApiClient apiClient = new ApiClient(mtlsWebClientFactory.createMtlsWebClient());

    apiClient.setBasePath(basePath);

    return apiClient;
  }

}
