package it.gov.pagopa.rtp.activator.configuration;

import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component("envVarsSslContextPropsProvider")
public class EnvVarsSslContextPropsProvider implements SslContextPropsProvider {

  private final SslContextProps sslContextProps;


  public EnvVarsSslContextPropsProvider(@NonNull final SslContextProps sslContextProps) {
    this.sslContextProps = Objects.requireNonNull(sslContextProps);
  }


  @NonNull
  @Override
  public SslContextProps getSslContextProps() {
    return this.sslContextProps;
  }
}
