package it.gov.pagopa.rtp.activator.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ActivationPropertiesConfig.class)
@TestPropertySource("classpath:application.yaml")
class ActivationPropertiesConfigTest {

  @Autowired private ActivationPropertiesConfig activationPropertiesConfig;

  @Test
  void testPropertiesLoaded() {
    assertNotNull(activationPropertiesConfig);
    assertEquals("http://localhost:8080/", activationPropertiesConfig.baseUrl());
  }
}
