package it.gov.pagopa.rtp.activator.telemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Interceptor for adding OpenTelemetry tracing to reactive MongoDB operations.
 * This class intercepts method calls to ReactiveMongoRepository implementations
 * and creates spans to track MongoDB operations for observability purposes.
 */
public class ReactiveMongoTraceInterceptor implements MethodInterceptor {

    private final Tracer tracer;
    private final ReactiveMongoTemplate mongoTemplate;
    private final List<String> methodsToIgnore;

    /**
     * Constructs a new ReactiveMongoTraceInterceptor.
     *
     * @param tracer        OpenTelemetry tracer for creating spans
     * @param mongoTemplate Template for MongoDB operations
     */
    public ReactiveMongoTraceInterceptor(Tracer tracer, ReactiveMongoTemplate mongoTemplate) {
        this.tracer = tracer;
        this.mongoTemplate = mongoTemplate;
        this.methodsToIgnore = List.of("getMongoTemplate", "getMongoDB");
    }

    /**
     * Extracts the repository name from a repository instance.
     * Attempts to find the specific ReactiveMongoRepository interface
     * implementation.
     *
     * @param repository The repository instance
     * @return The simple name of the repository interface
     */
    private String getRepositoryName(Object repository) {
        // Get all interfaces implemented by the proxy
        Class<?>[] interfaces = repository.getClass().getInterfaces();

        // Find the repository interface
        return java.util.Arrays.stream(interfaces)
                .filter(iface -> ReactiveMongoRepository.class.isAssignableFrom(iface))
                .findFirst()
                .map(Class::getSimpleName)
                .orElse(repository.getClass().getSimpleName());
    }

    /**
     * Converts a repository name to a collection name by converting from CamelCase
     * to snake_case and removing the "Repository" suffix.
     *
     * @param repositoryName Name of the repository class
     * @return The corresponding MongoDB collection name
     */
    private String getCollectionName(String repositoryName) {
        // Convert from CamelCase to snake_case and remove "Repository" suffix
        return repositoryName
                .replaceAll("Repository$", "")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    /**
     * Extracts query details from a method invocation, including both
     * explicit @Query
     * annotations and derived query methods, along with their parameters.
     *
     * @param method The intercepted method
     * @param args   The method arguments
     * @return A string containing the query details
     */
    protected String extractQueryDetails(Method method, Object[] args) {
        StringBuilder queryInfo = new StringBuilder();

        // Check for @Query annotation
        Query queryAnnotation = AnnotationUtils.findAnnotation(method, Query.class);
        if (queryAnnotation != null) {
            queryInfo.append("Query: ").append(queryAnnotation.value());
        } else {
            // For derived query methods, convert method name to query representation
            queryInfo.append("Derived: ").append(method.getName());
        }   

        return queryInfo.toString();
    }

    /**
     * Main interceptor method that handles method invocations.
     * Checks for TraceMongo annotation and applies tracing if present.
     *
     * @param invocation The method invocation being intercepted
     * @return The result of the method invocation
     * @throws Throwable if the invocation fails
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        TraceMongo traceMongo = AnnotationUtils.findAnnotation(invocation.getMethod(), TraceMongo.class);
        if (traceMongo == null) {
            traceMongo = AnnotationUtils.findAnnotation(invocation.getThis().getClass(), TraceMongo.class);
        }
        if (traceMongo != null) {
            // Apply tracing logic
            return tracingLogic(invocation);
        } else {
            // Skip tracing
            return invocation.proceed();
        }
    }

    /**
     * Implements the core tracing logic for MongoDB operations.
     * Creates and manages OpenTelemetry spans for tracking MongoDB operations,
     * including success and error scenarios.
     *
     * @param invocation The method invocation to be traced
     * @return A Mono containing the result of the operation
     * @throws Throwable if the operation fails
     */
    private Object tracingLogic(MethodInvocation invocation) throws Throwable {
        // Check if the target is a ReactiveMongoRepository implementation
        if (!(invocation.getThis() instanceof ReactiveMongoRepository<?, ?>)) {
            return invocation.proceed();
        }

        ReactiveMongoRepository<?, ?> repository = (ReactiveMongoRepository<?, ?>) invocation.getThis();
        Method method = invocation.getMethod();
        String methodName = method.getName();

        if (methodsToIgnore.contains(methodName)) {
            return invocation.proceed();
        }

        String repositoryName = getRepositoryName(repository);
        String collectionName = getCollectionName(repositoryName);
        String queryDetails = extractQueryDetails(method, invocation.getArguments());

        return Mono.deferContextual(ctx -> {
            // Create and configure the OpenTelemetry span
            Span span = tracer.spanBuilder(repositoryName + "." + methodName)
                    .setParent(Context.current().with(Span.current()))
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute("db.system", "mongodb")
                    .setAttribute("db.operation", methodName)
                    .setAttribute("db.mongodb.collection", collectionName)
                    .setAttribute("db.statement", queryDetails)
                    .startSpan();
                    
            // Execute the MongoDB operation with tracing
            return mongoTemplate.getMongoDatabase()
                    .switchIfEmpty(Mono.error(new IllegalStateException("Database not available")))
                    .flatMap(database -> {
                        span.setAttribute("db.name", database.getName());

                        try {
                            return ((Mono<?>) invocation.proceed())
                                    .doOnSubscribe(subscription -> span.addEvent("MongoDB operation started"))
                                    .doOnSuccess(result -> span.addEvent("MongoDB operation completed successfully"))
                                    .doOnError(error -> {
                                        span.recordException(error);
                                        span.setStatus(StatusCode.ERROR,
                                                "MongoDB operation failed: " + error.getMessage());
                                    })
                                    .doFinally(signalType -> span.end());
                        } catch (Throwable e) {
                            span.recordException(e);
                            span.setStatus(StatusCode.ERROR, "MongoDB operation failed: " + e.getMessage());
                            span.end();
                            return Mono.error(e);
                        }
                    });
        });
    }
}