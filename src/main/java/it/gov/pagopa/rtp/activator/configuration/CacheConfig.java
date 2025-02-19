package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.configuration.CachesConfigProperties.CacheConfigProperties;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

@Configuration
@EnableCaching
public class CacheConfig {

  private final CachesConfigProperties cacheProperties;
  private final CaffeineCacheFactory cacheFactory;


  @Autowired
  public CacheConfig(
      @NonNull final CachesConfigProperties cacheProperties,
      @NonNull final CaffeineCacheFactory cacheFactory) {

    this.cacheProperties = Objects.requireNonNull(cacheProperties,
        "Cache properties cannot be null");
    this.cacheFactory = Objects.requireNonNull(cacheFactory, "Cache factory cannot be null");
  }


  @NonNull
  @Bean(name = "caffeineCacheManager")
  public CacheManager cacheManager() {
    final var cacheManager = new CaffeineCacheManager();

    if (!CollectionUtils.isEmpty(cacheProperties.getCaches())) {

      final var cacheMap = cacheProperties.getCaches()
          .stream()
          .collect(Collectors.toConcurrentMap(
              CacheConfigProperties::name,
              cache -> cacheFactory.createCache(
                  cache.maximumSize(),
                  cache.expireAfterWrite()
              )
          ));

      cacheManager.setCacheLoader(
          cacheName -> Optional.of(cacheMap)
              .map(map -> map.get(cacheName.toString()))
              .orElseThrow(() -> new IllegalArgumentException("Cache not found: " + cacheName))
      );
    }

    return cacheManager;
  }
}

