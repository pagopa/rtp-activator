package it.gov.pagopa.rtp.activator.service.callback;


import com.fasterxml.jackson.databind.JsonNode;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.utils.IdentifierUtils;
import it.gov.pagopa.rtp.activator.utils.JsonNodeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CallbackFieldsExtractor {

    @NonNull
    public static List<TransactionStatus> exstractTransactionStatusSend (@NonNull final JsonNode responseNode) {
        JsonNode orgnlPmtInfAndSts = Optional.of(responseNode)
                .map(r -> r.path("AsynchronousSepaRequestToPayResponse")
                        .path("Document")
                        .path("CdtrPmtActvtnReqStsRpt")
                        .path("OrgnlPmtInfAndSts"))
                .orElseThrow(() -> new IllegalArgumentException("Missing OrgnlPmtInfAndSts field"));

        return JsonNodeUtils.toStream(orgnlPmtInfAndSts)
                .flatMap(pmt -> JsonNodeUtils.toStream(pmt.path("TxInfAndSts")))
                .map(t -> t.path("TxSts").asText(null))
                .map(StringUtils::trimToNull)
                .map(txtSt -> {
                    try{
                        return TransactionStatus.fromString(txtSt);
                    } catch (IllegalArgumentException e) {
                        return TransactionStatus.ERROR;
                    }
                })
                .collect(Collectors.toList());
    }

    @NonNull
    public static ResourceID exstractResourceIDSend (@NonNull final JsonNode responseNode) {
        return Optional.of(responseNode)
                .map(r -> r.path("AsynchronousSepaRequestToPayResponse")
                        .path("Document")
                        .path("CdtrPmtActvtnReqStsRpt")
                        .path("OrgnlGrpInfAndSts")
                        .path("OrgnlMsgId")
                        .asText())
                .map(StringUtils::trimToNull)
                .map(IdentifierUtils::uuidRebuilder)
                .map(ResourceID::new)
                .orElseThrow(() -> new IllegalArgumentException("Missing OrgnlGrpInfAndSts field"));
    }

}
