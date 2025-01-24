package it.gov.pagopa.rtp.activator;

import it.gov.pagopa.rtp.activator.configuration.ActivationPropertiesConfig;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableConfigurationProperties({ActivationPropertiesConfig.class, ServiceProviderConfig.class})
public class RtpActivatorApplication {

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(RtpActivatorApplication.class, args);
	}

}