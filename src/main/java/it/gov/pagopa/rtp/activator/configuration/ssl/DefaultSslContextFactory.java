package it.gov.pagopa.rtp.activator.configuration.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating an {@link SslContext} instance using a PKCS12
 * keystore.
 * <p>
 * This class loads SSL configuration from {@link SslContextProps}, decodes the
 * PFX file, decodes the trust store certificate, and initializes the keystore
 * and trust store. It then sets up a key manager factory and trust manager
 * factory for secure SSL connections.
 * </p>
 */
@Component("sslContextFactory")
@Slf4j
public class DefaultSslContextFactory implements SslContextFactory {

  /**
   * Holds SSL context configuration properties such as the PFX file, PFX
   * password, trust store certificate and password.
   */
  private final SslContextProps sslContextProps;

  /**
   * Constructs an instance of {@code DefaultSslContextFactory} using the provided
   * {@link SslContextPropsProvider}.
   *
   * @param sslContextPropsProvider the provider for SSL context properties.
   * @throws SslContextCreationException if SSL context properties cannot be
   *                                     retrieved.
   */
  public DefaultSslContextFactory(
      @NonNull final SslContextPropsProvider sslContextPropsProvider) {
    this.sslContextProps = Optional.of(sslContextPropsProvider)
        .map(SslContextPropsProvider::getSslContextProps)
        .orElseThrow(() -> new SslContextCreationException("Error getting SSL context props"));
  }

  /**
   * Creates and returns an {@link SslContext} instance.
   *
   * @return an initialized {@link SslContext}.
   * @throws SslContextCreationException if there is an error during SSL context
   *                                     creation.
   */
  @NonNull
  @Override
  public SslContext getSslContext() {
    KeyManagerFactory keyManagerFactory = Optional.of(this.initKeyStore())
        .map(this::initKeyManagerFactory)
        .orElseThrow(() -> new SslContextCreationException("Error creating key manager factory"));

    TrustManagerFactory trustManagerFactory = Optional.of(this.initTrustStore())
        .map(this::initTrustManagerFactory)
        .orElseThrow(() -> new SslContextCreationException("Error creating trust manager factory"));

    return this.initSSLContext(keyManagerFactory, trustManagerFactory);
  }

  /**
   * Converts a Base64-encoded file into an {@link InputStream}.
   *
   * @param base64PfxFile the Base64-encoded file.
   * @return an {@link InputStream} representing the decoded file.
   * @throws SslContextCreationException if decoding fails.
   */
  @NonNull
  private InputStream convertBase64FileToInputStream(@NonNull final String base64File) {
    return Optional.of(base64File)
        .map(Base64.getMimeDecoder()::decode)
        .map(ByteArrayInputStream::new)
        .orElseThrow(() -> new SslContextCreationException("Error decoding base64 file on JKS process"));
  }

  /**
   * Initializes a {@link KeyStore} instance from the PFX file.
   *
   * @return an initialized {@link KeyStore}.
   * @throws SslContextCreationException if keystore loading fails.
   */
  @NonNull
  private KeyStore initKeyStore() {

    try (final var keyStoreInputStream = this.convertBase64FileToInputStream(
        this.sslContextProps.pfxFile())) {

      final var keyStore = KeyStore.getInstance(this.sslContextProps.pfxType());
      final var password = this.sslContextProps.pfxPassword().toCharArray();

      keyStore.load(keyStoreInputStream, password);
      return keyStore;

    } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
      log.error("Error loading keystore", e);
      throw new SslContextCreationException(e);
    }
  }

  /**
   * Initializes a {@link KeyStore} instance from the trust store certificate.
   *
   * @return an initialized {@link KeyStore}.
   * @throws SslContextCreationException if keystore loading fails.
   */
  @NonNull
  private KeyStore initTrustStore() {

    try (final var keyStoreInputStream = this.convertBase64FileToInputStream(
        this.sslContextProps.trustStoreCertificate())) {

      final var keyStore = KeyStore.getInstance(this.sslContextProps.trustStoreType());
      final var password = this.sslContextProps.trustStorePassword().toCharArray();

      keyStore.load(keyStoreInputStream, password);
      return keyStore;

    } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
      log.error("Error loading trust store", e);
      throw new SslContextCreationException(e);
    }
  }

  /**
   * Initializes a {@link KeyManagerFactory} using the provided keystore.
   *
   * @param keyStore the initialized {@link KeyStore}.
   * @return an initialized {@link KeyManagerFactory}.
   * @throws SslContextCreationException if key manager initialization fails.
   */
  @NonNull
  private KeyManagerFactory initKeyManagerFactory(@NonNull final KeyStore keyStore) {
    Objects.requireNonNull(keyStore, "Key store cannot be null");

    try {
      final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
          KeyManagerFactory.getDefaultAlgorithm());
      final var password = this.sslContextProps.pfxPassword().toCharArray();

      keyManagerFactory.init(keyStore, password);
      return keyManagerFactory;

    } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
      log.error("Error creating key manager factory", e);
      throw new SslContextCreationException(e);
    }
  }

  /**
   * Initializes a {@link TrustManagerFactory} using the provided keystore.
   *
   * @param keyStore the initialized {@link KeyStore}.
   * @return an initialized {@link TrustManagerFactory}.
   * @throws SslContextCreationException if trust manager initialization fails.
   */
  @NonNull
  private TrustManagerFactory initTrustManagerFactory(@NonNull final KeyStore keyStore) {
    Objects.requireNonNull(keyStore, "Key store cannot be null");

    try {
      final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());

      trustManagerFactory.init(keyStore);
      return trustManagerFactory;

    } catch (NoSuchAlgorithmException | KeyStoreException e) {
      log.error("Error creating trust manager factory", e);
      throw new SslContextCreationException(e);
    }
  }

  /**
   * Initializes and returns an {@link SslContext} using the given key managers.
   *
   * @param keyManagerFactory an array of {@link KeyManagerFactory} instances.
   * @return an initialized {@link SslContext}.
   * @throws SslContextCreationException if SSL context initialization fails.
   */
  @NonNull
  private SslContext initSSLContext(@NonNull final KeyManagerFactory keyManagerFactory,
      @NonNull final TrustManagerFactory trustManagerFactory) {
    Objects.requireNonNull(keyManagerFactory, "Key manager factory cannot be null");
    Objects.requireNonNull(trustManagerFactory, "Trust manager factory cannot be null");

    try {
      return SslContextBuilder.forClient()
          .keyManager(keyManagerFactory)
          .trustManager(trustManagerFactory)
          .protocols("TLSv1.2", "TLSv1.3")
          .build();

    } catch (SSLException e) {
      log.error("Error creating SSL context", e);
      throw new SslContextCreationException(e);
    }
  }
}
