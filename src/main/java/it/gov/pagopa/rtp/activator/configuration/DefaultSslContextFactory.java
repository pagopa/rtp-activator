package it.gov.pagopa.rtp.activator.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


@Component("sslContextFactory")
@Slf4j
public class DefaultSslContextFactory implements SslContextFactory {

  private final SslContextProps sslContextProps;


  public DefaultSslContextFactory(@NonNull final SslContextPropsProvider sslContextPropsProvider) {
    this.sslContextProps = Optional.of(sslContextPropsProvider)
        .map(SslContextPropsProvider::getSslContextProps)
        .orElseThrow(() -> new SslContextCreationException("Error getting ssl context props"));
  }


  @NonNull
  @Override
  public SSLContext getSslContext() {
    return Optional.of(this.initKeyStore())
        .map(this::initKeyManagerFactory)
        .map(KeyManagerFactory::getKeyManagers)
        .map(this::initSSLContext)
        .orElseThrow(() -> new SslContextCreationException("Error creating ssl context"));
  }


  @NonNull
  private InputStream convertPfxFileToInputStream(@NonNull final String base64PfxFile) {
    return Optional.of(base64PfxFile)
        .map(Base64.getMimeDecoder()::decode)
        .map(ByteArrayInputStream::new)
        .orElseThrow(() -> new SslContextCreationException("Error decoding pfx file"));
  }


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


  @NonNull
  private KeyManagerFactory initKeyManagerFactory(
      @NonNull final KeyStore keyStore) {

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


  @NonNull
  private SSLContext initSSLContext(
      @NonNull final KeyManager[] keyManagers) {
    Objects.requireNonNull(keyManagers, "Key managers cannot be null");

    final SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance(this.sslContextProps.protocol());
      sslContext.init(keyManagers, null, null);
      return sslContext;

    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      log.error("Error creating ssl context", e);
      throw new SslContextCreationException(e);
    }
  }

}
