package it.gov.pagopa.rtp.activator.configuration.ssl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;


@SpringBootTest
class DefaultSslContextFactoryTest {

  private final SslContextProps sslContextProps;


  @Autowired
  public DefaultSslContextFactoryTest(
      @NonNull final SslContextProps sslContextProps
  ) {
    this.sslContextProps = sslContextProps;
  }

  @Test
  void givenValidSslProps_whenGetSslContext_thenReturnValidSslContext() {
    final var sslContextFactory = new DefaultSslContextFactory(() -> this.sslContextProps);

    final var sslContext = sslContextFactory.getSslContext();

    assertNotNull(sslContext);
  }


  @Test
  void givenInvalidPfx_whenGetSslContext_thenThrowIllegalArgumentException() {
    final var inputSslContextProps = this.sslContextProps.withPfxFile("invalid-base64");

    final var sslContextFactory = new DefaultSslContextFactory(() -> inputSslContextProps);

    assertThrows(IllegalArgumentException.class, sslContextFactory::getSslContext);
  }


  @Test
  void givenInvalidPassword_whenGetSslContext_thenThrowException() {
    final var inputSslContextProps = this.sslContextProps.withPfxPassword("invalid-password");

    final var sslContextFactory = new DefaultSslContextFactory(() -> inputSslContextProps);

    assertThrows(SslContextCreationException.class, sslContextFactory::getSslContext);
  }


  @Test
  void givenNullPfxPassword_whenGetSslContext_thenThrowNullPointerException() {
    final var inputSslContextProps = this.sslContextProps.withPfxPassword(null);

    final var sslContextFactory = new DefaultSslContextFactory(() -> inputSslContextProps);

    assertThrows(NullPointerException.class, sslContextFactory::getSslContext);
  }


  @Test
  void givenInvalidPfxType_whenGetSslContext_thenSslContextCreationException() {
    final var inputSslContextProps = this.sslContextProps.withPfxType("invalid-pfx-type");

    final var sslContextFactory = new DefaultSslContextFactory(() -> inputSslContextProps);

    assertThrows(SslContextCreationException.class, sslContextFactory::getSslContext);
  }

}
