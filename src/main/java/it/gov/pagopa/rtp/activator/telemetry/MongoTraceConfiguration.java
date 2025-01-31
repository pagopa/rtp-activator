package it.gov.pagopa.rtp.activator.telemetry;

import io.opentelemetry.api.trace.Tracer;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Configuration class for MongoDB tracing functionality.
 * This class sets up the necessary components to enable OpenTelemetry tracing
 * for MongoDB Reactive operations in a reactive application context.
 */
@Configuration
public class MongoTraceConfiguration {

  /**
   * Creates a trace interceptor bean for MongoDB operations.
   * This interceptor is responsible for creating and managing trace spans
   * for MongoDB database operations.
   *
   * @param tracer        OpenTelemetry tracer for creating spans
   * @param mongoTemplate Template for MongoDB operations
   * @return A new instance of ReactiveMongoTraceInterceptor
   */
  @Bean
  public ReactiveMongoTraceInterceptor mongoTraceInterceptor(
      Tracer tracer,
      ReactiveMongoTemplate mongoTemplate) {
    return new ReactiveMongoTraceInterceptor(tracer, mongoTemplate);
  }

  /**
   * Creates a bean post processor that adds tracing capabilities to MongoDB
   * repositories.
   * This processor automatically creates proxies for all ReactiveMongoRepository
   * instances,
   * adding tracing interceptors to track database operations.
   *
   * @param tracer        OpenTelemetry tracer for creating spans
   * @param mongoTemplate Template for MongoDB operations
   * @return BeanPostProcessor that creates proxies for repository beans
   */
  @Bean
  public BeanPostProcessor mongoRepositoryProxyPostProcessor(
      Tracer tracer, ReactiveMongoTemplate mongoTemplate) {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        // Only create proxies for ReactiveMongoRepository implementations
        if (bean instanceof ReactiveMongoRepository) {
          // Create and configure the proxy with tracing interceptor
          ProxyFactory proxyFactory = new ProxyFactory(bean);
          proxyFactory.addAdvice(new ReactiveMongoTraceInterceptor(tracer, mongoTemplate));
          return proxyFactory.getProxy();
        }
        return bean;
      }
    };
  }

}