package it.gov.pagopa.rtp.activator.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Data
@RequiredArgsConstructor
@Validated
@ConfigurationProperties(prefix = "cache")
public class CachesConfigProperties {

  private final List<CacheConfigProperties> params;

  @With
  public record CacheConfigProperties(@NotBlank String name, @Positive Integer maximumSize, Duration expireAfterWrite) {}
}

