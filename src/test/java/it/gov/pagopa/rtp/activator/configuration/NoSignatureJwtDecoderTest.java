package it.gov.pagopa.rtp.activator.configuration;

import com.nimbusds.jose.JOSEException;
import it.gov.pagopa.rtp.activator.JwtTestUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class NoSignatureJwtDecoderTest {


    @Test
    void givenSignedTokenMustDecodeWithoutVerifySignature() throws JOSEException {
        final var decoder = new NoSignatureJwtDecoder();
        final var token = JwtTestUtils.generateToken("me", "none");
        assertThat(decoder.decode(token), Matchers.notNullValue());
    }

}

