package it.gov.pagopa.rtp.activator.utils;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonNodeUtils {

    @NonNull
    public static Collection<JsonNode> nodeToCollection(@NonNull final JsonNode node) {
        return Optional.of(node)
                .filter(JsonNode::isContainerNode)
                .map(n -> n.isArray()
                        ? StreamSupport.stream(n.spliterator(), false)
                        : Stream.of(n))
                .map(Stream::toList)
                .orElseGet(List::of);
    }
}
