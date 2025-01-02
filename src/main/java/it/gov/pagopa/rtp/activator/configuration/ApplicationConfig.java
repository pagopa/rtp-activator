package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  public ReadApi readApi() {
    return new ReadApi();
  }

}
