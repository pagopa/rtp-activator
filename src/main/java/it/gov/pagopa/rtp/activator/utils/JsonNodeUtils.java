package it.gov.pagopa.rtp.activator.utils;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Utility class for working with Jackson {@link JsonNode} structures in reactive contexts.
 *
 * <p>This class provides methods to help convert {@link JsonNode} containers into reactive streams,
 * particularly useful when traversing dynamic JSON trees using Project Reactor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonNodeUtils {

    /**
     * Converts a given {@link JsonNode} into a {@link Flux} of child nodes.
     *
     * <p>If the provided node is an array, each element in the array will be emitted as a separate
     * item in the flux. If the node is an object or a primitive container, the node itself will be emitted
     * as a single item. If the node is not a container (e.g., null, missing, or scalar), an empty flux is returned.
     *
     * @param node the {@link JsonNode} to convert (must not be null)
     * @return a {@link Flux} of {@link JsonNode} elements, one for each item in the array or the node itself
     */
    public static Flux<JsonNode> nodeToFlux(@NonNull final JsonNode node) {
        return Optional.of(node)
                .filter(JsonNode::isContainerNode)
                .map(n -> n.isArray()
                        ? StreamSupport.stream(n.spliterator(), false)
                        : Stream.of(n))
                .map(Flux::fromStream)
                .orElseGet(Flux::empty);
    }
}
