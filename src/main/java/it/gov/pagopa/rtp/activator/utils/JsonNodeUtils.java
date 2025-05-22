package it.gov.pagopa.rtp.activator.utils;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonNodeUtils {

    public static Stream<JsonNode> toStream(@NonNull JsonNode node) {
        return Optional.of(node)
                .filter(JsonNode::isContainerNode)
                .map(n -> n.isArray()
                        ? StreamSupport.stream(n.spliterator(), false)
                        : Stream.of(n))
                .orElseGet(Stream::empty);
    }
}
