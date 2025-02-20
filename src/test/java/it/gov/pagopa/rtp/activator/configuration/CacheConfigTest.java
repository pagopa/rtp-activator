package it.gov.pagopa.rtp.activator.configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Caffeine;
import it.gov.pagopa.rtp.activator.configuration.CachesConfigProperties.CacheConfigProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.util.CollectionUtils;

@ExtendWith(MockitoExtension.class)
class CacheConfigTest {

  @Mock
  private CachesConfigProperties cacheProperties;

  @Mock
  private CaffeineCacheFactory cacheFactory;

  @InjectMocks
  private CacheConfig cacheConfig;

  private List<CacheConfigProperties> cacheParams;

  @BeforeEach
  void setUp() {
    cacheParams = List.of(
        new CacheConfigProperties("testCache", 100, Duration.ofMinutes(10)),
        new CacheConfigProperties("anotherCache", 50, Duration.ofHours(1))
    );
  }

  @Test
  void givenValidCacheConfig_whenCacheManagerCreated_thenCachesRegisteredCorrectly() {
    when(cacheProperties.getParams()).thenReturn(cacheParams);
    when(cacheFactory.createCache(anyLong(), any())).thenAnswer(invocation -> Caffeine.newBuilder()
        .maximumSize(invocation.getArgument(0))
        .expireAfterWrite(invocation.getArgument(1)));

    CacheManager cacheManager = cacheConfig.cacheManager();
    assertInstanceOf(CaffeineCacheManager.class, cacheManager);

    CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;

    assertTrue(caffeineCacheManager.getCacheNames().contains("testCache"));
    assertTrue(caffeineCacheManager.getCacheNames().contains("anotherCache"));
  }

  @Test
  void givenEmptyCacheConfig_whenCacheManagerCreated_thenNoCachesRegistered() {
    when(cacheProperties.getParams()).thenReturn(List.of());

    CacheManager cacheManager = cacheConfig.cacheManager();
    assertInstanceOf(CaffeineCacheManager.class, cacheManager);

    CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
    assertTrue(CollectionUtils.isEmpty(caffeineCacheManager.getCacheNames()));
  }
}