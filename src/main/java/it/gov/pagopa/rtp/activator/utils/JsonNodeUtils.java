package it.gov.pagopa.rtp.activator.utils;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonNodeUtils {

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
