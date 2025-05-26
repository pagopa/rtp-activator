package it.gov.pagopa.rtp.activator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonNodeUtilsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void givenArrayNode_whenNodeToFlux_thenReturnFluxOfElements() throws IOException {
        String jsonArray = "[{\"field\": \"value1\"}, {\"field\": \"value2\"}]";
        JsonNode arrayNode = objectMapper.readTree(jsonArray);

        Flux<JsonNode> result = JsonNodeUtils.nodeToFlux(arrayNode);

        StepVerifier.create(result)
                .expectNextMatches(n -> n.get("field").asText().equals("value1"))
                .expectNextMatches(n -> n.get("field").asText().equals("value2"))
                .verifyComplete();
    }

    @Test
    void givenObjectNode_whenNodeToFlux_thenReturnFluxWithSingleElement() throws IOException {
        String jsonObject = "{\"field\": \"value\"}";
        JsonNode objectNode = objectMapper.readTree(jsonObject);

        Flux<JsonNode> result = JsonNodeUtils.nodeToFlux(objectNode);

        StepVerifier.create(result)
                .expectNextMatches(n -> n.get("field").asText().equals("value"))
                .verifyComplete();
    }

    @Test
    void givenValueNode_whenNodeToFlux_thenReturnEmptyFlux() throws IOException {
        String jsonValue = "\"just a string\"";
        JsonNode valueNode = objectMapper.readTree(jsonValue);

        Flux<JsonNode> result = JsonNodeUtils.nodeToFlux(valueNode);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void givenNullInput_whenNodeToFlux_thenThrowNullPointerException() {
        JsonNode nullNode = null;

        assertThrows(NullPointerException.class, () -> JsonNodeUtils.nodeToFlux(nullNode));
    }
}
