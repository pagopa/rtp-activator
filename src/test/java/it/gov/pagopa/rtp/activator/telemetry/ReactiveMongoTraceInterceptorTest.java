//package it.gov.pagopa.rtp.activator.telemetry;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.contains;
//import static org.mockito.Mockito.*;
//
//import java.lang.reflect.Method;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
//import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
//
//import com.mongodb.reactivestreams.client.MongoDatabase;
//
//import io.opentelemetry.api.trace.Span;
//import io.opentelemetry.api.trace.SpanBuilder;
//import io.opentelemetry.api.trace.Tracer;
//
//
//import org.aopalliance.intercept.MethodInvocation;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//class ReactiveMongoTraceInterceptorTest {
//
//    @Test
//    void invokeSkipsTracingWhenAnnotationAbsent() throws Throwable {
//        Tracer tracer = mock(Tracer.class);
//        ReactiveMongoTemplate mongoTemplate = mock(ReactiveMongoTemplate.class);
//        MethodInvocation invocation = mock(MethodInvocation.class);
//
//        ReactiveMongoRepository<?, ?> mockRepo = mock(ReactiveMongoRepository.class);
//
//        when(invocation.getMethod()).thenReturn(Object.class.getMethod("toString"));
//        when(invocation.getThis()).thenReturn(mockRepo);
//        when(invocation.proceed()).thenReturn(Mono.just("data"));
//
//        ReactiveMongoTraceInterceptor interceptor = new ReactiveMongoTraceInterceptor(tracer, mongoTemplate);
//        interceptor.invoke(invocation);
//
//        verify(invocation).proceed();
//        verifyNoInteractions(tracer);
//    }
//
//    @Test
//    void invokeAppliesTracingWhenAnnotationPresent() throws Throwable {
//        Tracer tracer = mock(Tracer.class);
//        SpanBuilder spanBuilder = mock(SpanBuilder.class);
//        Span span = mock(Span.class);
//        ReactiveMongoTemplate mongoTemplate = mock(ReactiveMongoTemplate.class);
//        MethodInvocation invocation = mock(MethodInvocation.class);
//        Method method = TestRepository.class.getMethod("findById", String.class);
//
//        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
//        when(spanBuilder.setParent(any())).thenReturn(spanBuilder);
//        when(spanBuilder.setSpanKind(any())).thenReturn(spanBuilder);
//        when(spanBuilder.setAttribute(anyString(), any())).thenReturn(spanBuilder);
//        when(spanBuilder.startSpan()).thenReturn(span);
//        when(mongoTemplate.getMongoDatabase()).thenReturn(Mono.just(mock(MongoDatabase.class)));
//        when(invocation.getMethod()).thenReturn(method);
//        when(invocation.getThis()).thenReturn(mock(ReactiveMongoRepository.class));
//        when(invocation.getArguments()).thenReturn(new Object[] { "id" });
//        when(invocation.proceed()).thenReturn(Mono.just("data"));
//
//        ReactiveMongoTraceInterceptor interceptor = new ReactiveMongoTraceInterceptor(tracer, mongoTemplate);
//        Mono<Object> result = (Mono<Object>) interceptor.invoke(invocation);
//
//        StepVerifier.create(result).expectNext((Object) "data").verifyComplete();
//        verify(tracer).spanBuilder(contains("ReactiveMongoRepository.findById"));
//        verify(span).end();
//    }
//
//    @Test
//    void tracingLogicIgnoresMethodsInIgnoreList() throws Throwable {
//        Tracer tracer = mock(Tracer.class);
//        MethodInvocation invocation = mock(MethodInvocation.class);
//        Method method = TestRepository.class.getMethod("getMongoTemplate");
//        ReactiveMongoRepository<?, ?> mockRepo = mock(ReactiveMongoRepository.class);
//
//        when(invocation.getMethod()).thenReturn(method);
//        when(invocation.proceed()).thenReturn(Mono.empty());
//        when(invocation.getThis()).thenReturn(mockRepo);
//
//        ReactiveMongoTraceInterceptor interceptor = new ReactiveMongoTraceInterceptor(mock(Tracer.class),
//                mock(ReactiveMongoTemplate.class));
//        interceptor.invoke(invocation);
//
//        verify(invocation).proceed();
//        verifyNoInteractions(tracer);
//    }
//
//    @Test
//    void extractQueryDetailsUsesQueryAnnotation() throws NoSuchMethodException {
//        Tracer tracer = mock(Tracer.class);
//        ReactiveMongoTemplate mongoTemplate = mock(ReactiveMongoTemplate.class);
//        ReactiveMongoTraceInterceptor interceptor = new ReactiveMongoTraceInterceptor(tracer, mongoTemplate);
//
//        Method method = QueryAnnotatedRepository.class.getMethod("findByCustomQuery");
//
//        String queryDetails = interceptor.extractQueryDetails(method, new Object[] {});
//
//        assertTrue(queryDetails.contains("Query: customQuery"),
//                "Expected query details to include the custom query from the annotation");
//    }
//
//    public interface QueryAnnotatedRepository extends ReactiveMongoRepository<Object, String> {
//        @Query("customQuery")
//        Mono<Object> findByCustomQuery();
//
//    }
//
//    public interface TestRepository extends ReactiveMongoRepository<Object, String> {
//        @TraceMongo
//        Mono<Object> findById(String id);
//        Mono<Object> getMongoTemplate();
//    }
//
//}