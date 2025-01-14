package it.gov.pagopa.rtp.activator.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.JOSEException;
import it.gov.pagopa.rtp.activator.utils.JwtUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtValidationException;

class NoSignatureJwtDecoderTest {

  @Test
  void givenSignedTokenMustDecodeWithoutVerifySignature() throws JOSEException {
    final var decoder = new NoSignatureJwtDecoder();
    final var token = JwtUtils.generateToken("me", "none");
    assertThat(decoder.decode(token), Matchers.notNullValue());
  }

  @Test
  void givenExpiredTokenMustThrowError() throws JOSEException {
    final var decoder = new NoSignatureJwtDecoder();
    final var token = JwtUtils.generateExpiredToken("me", "none");
    assertThrows(JwtValidationException.class, () -> decoder.decode(token));
  }
}
