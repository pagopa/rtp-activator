package it.gov.pagopa.rtp.activator.configuration.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


/**
 * Factory class for creating an {@link SslContext} instance using a PKCS12 keystore.
 * <p>
 * This class loads SSL configuration from {@link SslContextProps}, decodes the PFX file,
 * initializes the keystore, and sets up a key manager factory for secure SSL connections.
 * </p>
 */
@Component("sslContextFactory")
@Slf4j
public class DefaultSslContextFactory implements SslContextFactory {

  /**
   * Holds SSL context configuration properties such as the PFX file and password.
   */
  private final SslContextProps sslContextProps;

  /**
   * Constructs an instance of {@code DefaultSslContextFactory} using the provided
   * {@link SslContextPropsProvider}.
   *
   * @param sslContextPropsProvider the provider for SSL context properties.
   * @throws SslContextCreationException if SSL context properties cannot be retrieved.
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
   * @throws SslContextCreationException if there is an error during SSL context creation.
   */
  @NonNull
  @Override
  public SslContext getSslContext() {
    return Optional.of(this.initKeyStore())
        .map(this::initKeyManagerFactory)
        .map(this::initSSLContext)
        .orElseThrow(() -> new SslContextCreationException("Error creating SSL context"));
  }

  /**
   * Converts a Base64-encoded PFX file into an {@link InputStream}.
   *
   * @param base64PfxFile the Base64-encoded PFX file.
   * @return an {@link InputStream} representing the decoded PFX file.
   * @throws SslContextCreationException if decoding fails.
   */
  @NonNull
  private InputStream convertPfxFileToInputStream(@NonNull final String base64PfxFile) {
    return Optional.of(base64PfxFile)
        .map(Base64.getMimeDecoder()::decode)
        .map(ByteArrayInputStream::new)
        .orElseThrow(() -> new SslContextCreationException("Error decoding PFX file"));
  }

  /**
   * Initializes a {@link KeyStore} instance from the PFX file.
   *
   * @return an initialized {@link KeyStore}.
   * @throws SslContextCreationException if keystore loading fails.
   */
  @NonNull
  private KeyStore initKeyStore() {

    try (final var keyStoreInputStream = this.convertPfxFileToInputStream(
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
   * Initializes and returns an {@link SslContext} using the given key managers.
   *
   * @param keyManagerFactory an array of {@link KeyManagerFactory} instances.
   * @return an initialized {@link SslContext}.
   * @throws SslContextCreationException if SSL context initialization fails.
   */
  @NonNull
  private SslContext initSSLContext(@NonNull final KeyManagerFactory keyManagerFactory) {
    Objects.requireNonNull(keyManagerFactory, "Key manager factory cannot be null");

    try {
      return SslContextBuilder.forClient()
          .keyManager(keyManagerFactory)
          .trustManager(InsecureTrustManagerFactory.INSTANCE)
          .protocols("TLSv1.2","TLSv1.3")
          .build();

    } catch (SSLException e) {
      log.error("Error creating SSL context", e);
      throw new SslContextCreationException(e);
    }
  }
}

