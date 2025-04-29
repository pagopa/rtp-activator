package it.gov.pagopa.rtp.activator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.MDC;

public final class PayloadInfoExtractor {

    private PayloadInfoExtractor() {}

    public static void populateMdc(JsonNode payload) {
        JsonNode resp = payload.path("AsynchronousSepaRequestToPayResponse");
        JsonNode node = resp.has("Document")
                ? resp.path("Document").path("CdtrPmtActvtnReqStsRpt")
                : resp.path("CdtrPmtActvtnReqStsRpt");

        String serviceProvider = node
                .path("GrpHdr")
                .path("InitgPty")
                .path("Id")
                .path("OrgId")
                .path("AnyBIC")
                .asText(null);

        if (serviceProvider != null) {
            MDC.put("service_provider", serviceProvider);
        }

        String debtor = payload.path("fiscalCode").asText(null);
        if (debtor == null) {
            debtor = node
                    .path("OrgnlPmtInfAndSts")
                    .path(0)
                    .path("TxInfAndSts")
                    .path("OrgnlEndToEndId")
                    .asText(null);
        }
        if (debtor != null) {
            MDC.put("debtor", debtor);
        }
    }
}
