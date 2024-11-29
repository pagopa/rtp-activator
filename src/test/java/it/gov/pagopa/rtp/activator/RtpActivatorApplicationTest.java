package it.gov.pagopa.rtp.activator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class RtpActivatorApplicationTests {

	@Test
	void contextLoads() {
		assertNotNull(new RtpActivatorApplication());
	}	

}