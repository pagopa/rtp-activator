package it.gov.pagopa.rtp.activator.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;


@SpringBootTest
class DefaultSslContextFactoryTest {

  private final String pfxFileName;
  private final String pfxFilePassword;
  private final String pfxType;
  private final String protocol;


  public DefaultSslContextFactoryTest(
      @NonNull @Value("${client.ssl.pfx-name}") String pfxFileName,
      @NonNull @Value("${client.ssl.pfx-password}") String pfxFilePassword,
      @NonNull @Value("${client.ssl.pfx-type}") String pfxType,
      @NonNull @Value("${client.ssl.protocol}") String protocol
  ) {
    this.pfxFileName = pfxFileName;
    this.pfxFilePassword = pfxFilePassword;
    this.pfxType = pfxType;
    this.protocol = protocol;
  }

  @Test
  void givenValidSslProps_whenGetSslContext_thenReturnValidSslContext() {
    final var sslContextProps = new SslContextProps(
        getValidPfxBase64(),
        this.pfxFilePassword,
        this.pfxType,
        this.protocol
    );

    final var sslContextFactory = new DefaultSslContextFactory(() -> sslContextProps);

    final var sslContext = sslContextFactory.getSslContext();

    assertNotNull(sslContext);
    assertEquals("TLS", sslContext.getProtocol());
  }


  @Test
  void givenInvalidPfx_whenGetSslContext_thenThrowIllegalArgumentException() {
    final var sslContextProps = new SslContextProps(
        "invalid-base64",
        this.pfxFilePassword,
        this.pfxType,
        this.protocol
    );

    final var sslContextFactory = new DefaultSslContextFactory(() -> sslContextProps);

    assertThrows(IllegalArgumentException.class, sslContextFactory::getSslContext);
  }


  @Test
  void givenInvalidPassword_whenGetSslContext_thenThrowException() {
    final var sslContextProps = new SslContextProps(
        getValidPfxBase64(),
        "invalid-password",
        this.pfxType,
        this.protocol
    );

    final var sslContextFactory = new DefaultSslContextFactory(() -> sslContextProps);

    assertThrows(SslContextCreationException.class, sslContextFactory::getSslContext);
  }


  @Test
  void givenNullSslProps_whenInstantiate_thenThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> new DefaultSslContextFactory(null));
  }


  @Test
  void givenNullPfxFile_whenGetSslContext_thenThrowNullPointerException() {
    final var sslContextProps = new SslContextProps(
        null,
        this.pfxFilePassword,
        this.pfxType,
        this.protocol
    );

    final var sslContextFactory = new DefaultSslContextFactory(() -> sslContextProps);

    assertThrows(NullPointerException.class, sslContextFactory::getSslContext);
  }


  @Test
  void givenNullPfxPassword_whenGetSslContext_thenThrowNullPointerException() {
    final var sslContextProps = new SslContextProps(
        getValidPfxBase64(),
        null,
        this.pfxType,
        this.protocol
    );

    final var sslContextFactory = new DefaultSslContextFactory(() -> sslContextProps);

    assertThrows(NullPointerException.class, sslContextFactory::getSslContext);
  }


  private String getValidPfxBase64() {
    try {
      final var pfxBytes = new ClassPathResource(this.pfxFileName)
          .getInputStream()
          .readAllBytes();

      return Base64.getEncoder()
          .encodeToString(pfxBytes);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
