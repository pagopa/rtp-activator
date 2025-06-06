package it.gov.pagopa.rtp.activator;

import it.gov.pagopa.rtp.activator.configuration.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableConfigurationProperties({
    ActivationPropertiesConfig.class,
    ApplicationInsightsProperties.class,
})
public class RtpActivatorApplication {

  public static void main(String[] args) {
    Hooks.enableAutomaticContextPropagation();
    SpringApplication.run(RtpActivatorApplication.class, args);
  }

}