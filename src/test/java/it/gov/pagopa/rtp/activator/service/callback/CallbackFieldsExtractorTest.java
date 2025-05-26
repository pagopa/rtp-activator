package it.gov.pagopa.rtp.activator.service.callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;


class CallbackFieldsExtractorTest {

    private CallbackFieldsExtractor extractor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        extractor = new CallbackFieldsExtractor();
        objectMapper = new ObjectMapper();
    }

    @Test
    void givenValidJson_whenExtractTransactionStatusSend_thenReturnTransactionStatuses() throws Exception {
        String json = """
            {
              "AsynchronousSepaRequestToPayResponse": {
                "Document": {
                  "CdtrPmtActvtnReqStsRpt": {
                    "OrgnlPmtInfAndSts": [
                      {
                        "TxInfAndSts": [
                          {
                            "TxSts": ["ACCP"]
                          },
                          {
                            "TxSts": ["RJCT"]
                          }
                        ]
                      }
                    ]
                  }
                }
              }
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        var result = extractor.extractTransactionStatusSend(node);

        StepVerifier.create(result)
                .expectNext(TransactionStatus.ACCP)
                .expectNext(TransactionStatus.RJCT)
                .verifyComplete();
    }

    @Test
    void givenUnknownStatus_whenExtractTransactionStatusSend_thenReturnErrorStatus() throws Exception {
        String json = """
            {
              "AsynchronousSepaRequestToPayResponse": {
                "Document": {
                  "CdtrPmtActvtnReqStsRpt": {
                    "OrgnlPmtInfAndSts": [
                      {
                        "TxInfAndSts": [
                          {
                            "TxSts": ["FOO"]
                          }
                        ]
                      }
                    ]
                  }
                }
              }
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        var result = extractor.extractTransactionStatusSend(node);

        StepVerifier.create(result)
                .expectNext(TransactionStatus.ERROR)
                .verifyComplete();
    }

    @Test
    void givenMissingOrgnlPmtInfAndSts_whenExtractTransactionStatusSend_thenThrow() throws Exception {
        String json = """
            {
              "AsynchronousSepaRequestToPayResponse": {
                "Document": {
                  "CdtrPmtActvtnReqStsRpt": {}
                }
              }
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        var result = extractor.extractTransactionStatusSend(node);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof IllegalArgumentException &&
                                e.getMessage().equals("Missing field"))
                .verify();
    }

    @Test
    void givenValidJson_whenExtractResourceIDSend_thenReturnResourceID() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String json = """
            {
              "AsynchronousSepaRequestToPayResponse": {
                "Document": {
                  "CdtrPmtActvtnReqStsRpt": {
                    "OrgnlGrpInfAndSts": {
                      "OrgnlMsgId": "%s"
                    }
                  }
                }
              }
            }
        """.formatted(uuid);
        JsonNode node = objectMapper.readTree(json);

        var result = extractor.extractResourceIDSend(node);

        StepVerifier.create(result)
                .expectNextMatches(resourceID -> resourceID.getId().toString().equals(uuid))
                .verifyComplete();
    }

    @Test
    void givenMissingMsgId_whenExtractResourceIDSend_thenThrow() throws Exception {
        String json = """
            {
              "AsynchronousSepaRequestToPayResponse": {
                "Document": {
                  "CdtrPmtActvtnReqStsRpt": {
                    "OrgnlGrpInfAndSts": {}
                  }
                }
              }
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        var result = extractor.extractResourceIDSend(node);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof IllegalArgumentException &&
                                e.getMessage().equals("Missing field"))
                .verify();
    }

    @Test
    void givenInvalidUuid_whenExtractResourceIDSend_thenThrow() throws Exception {
        String json = """
            {
              "AsynchronousSepaRequestToPayResponse": {
                "Document": {
                  "CdtrPmtActvtnReqStsRpt": {
                    "OrgnlGrpInfAndSts": {
                      "OrgnlMsgId": "not-a-uuid"
                    }
                  }
                }
              }
            }
        """;
        JsonNode node = objectMapper.readTree(json);

        var result = extractor.extractResourceIDSend(node);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
