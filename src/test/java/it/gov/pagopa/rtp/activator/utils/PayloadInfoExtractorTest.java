package it.gov.pagopa.rtp.activator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

class PayloadInfoExtractorTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void populateMdc_withFirstFormat_shouldExtractServiceProviderOnly() throws Exception {
        String json = """
        {
          "resourceId": "456789123-rtp-response-001",
          "AsynchronousSepaRequestToPayResponse": {
            "CdtrPmtActvtnReqStsRpt": {
              "GrpHdr": {
                "MsgId": "RESPONSE-MSG-001",
                "CreDtTm": "2025-03-21T10:15:30",
                "InitgPty": {
                  "Id": {
                    "OrgId": {
                      "AnyBIC": "MOCKSP04"
                    }
                  }
                }
              },
              "OrgnlGrpInfAndSts": {
                "OrgnlMsgId": "ORIGINAL-REQ-001",
                "OrgnlMsgNmId": "pain.013.001.08",
                "OrgnlCreDtTm": "2025-03-20T14:30:00"
              }
            }
          },
          "_links": {
            "initialSepaRequestToPayUri": {
              "href": "https://api-rtp-cb.cstar.pagopa.it/rtp/cb/requests/123",
              "templated": false
            }
          }
        }
        """;
        JsonNode node = objectMapper.readTree(json);

        PayloadInfoExtractor.populateMdc(node);

        assertEquals("MOCKSP04", MDC.get("service_provider"));
        assertNull(MDC.get("debtor"));
    }

    @Test
    void populateMdc_withSecondFormat_shouldExtractServiceProviderAndDebtor() throws Exception {
        String jsonTemplate = """
        {
          "resourceId": "id",
          "AsynchronousSepaRequestToPayResponse": {
            "resourceId": "id",
            "Document": {
              "CdtrPmtActvtnReqStsRpt": {
                "GrpHdr": {
                  "MsgId": "MSG1",
                  "CreDtTm": "2025-03-21T10:15:30",
                  "InitgPty": { "Id": { "OrgId": { "AnyBIC": "%s" } } }
                },
                "OrgnlPmtInfAndSts": [
                  {
                    "OrgnlPmtInfId": "pid",
                    "TxInfAndSts": {
                      "StsId": "MSG1",
                      "OrgnlInstrId": "instr",
                      "OrgnlEndToEndId": "%s",
                      "TxSts": "RJCT",
                      "StsRsnInf": { "Orgtr": { "Id": { "OrgId": { "AnyBIC": "%s" } } } },
                      "OrgnlTxRef": { }
                    }
                  }
                ]
              }
            }
          }
        }
        """;
        String bic = "MOCKSP05";
        String debtorValue = "DEBTOR123";
        String json = String.format(jsonTemplate, bic, debtorValue, bic);
        JsonNode node = objectMapper.readTree(json);

        PayloadInfoExtractor.populateMdc(node);

        assertEquals(bic, MDC.get("service_provider"));
        assertEquals(debtorValue, MDC.get("debtor"));
    }

    @Test
    void populateMdc_withMissingFields_shouldNotThrow() throws Exception {
        String json = "{}";
        JsonNode node = objectMapper.readTree(json);

        assertDoesNotThrow(() -> PayloadInfoExtractor.populateMdc(node));
        assertNull(MDC.get("service_provider"));
        assertNull(MDC.get("debtor"));
    }
}
