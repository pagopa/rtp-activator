package it.gov.pagopa.rtp.activator.service.callback;


import com.fasterxml.jackson.databind.JsonNode;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.utils.IdentifierUtils;
import it.gov.pagopa.rtp.activator.utils.JsonNodeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component("callbackFieldsExtractor")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CallbackFieldsExtractor {

  @NonNull
  public List<TransactionStatus> extractTransactionStatusSend(@NonNull final JsonNode responseNode) {

      final var orgnlPmtInfAndSts = Optional.of(responseNode)
              .map(node -> node.path("AsynchronousSepaRequestToPayResponse"))
              .map(node -> node.path("Document"))
              .map(node -> node.path("CdtrPmtActvtnReqStsRpt"))
              .map(node -> node.path("OrgnlPmtInfAndSts"))
              .filter(node -> !node.isMissingNode())
              .orElseThrow(() -> new IllegalArgumentException("Missing field"));

      final var transactionStatusList = JsonNodeUtils.nodeToCollection(orgnlPmtInfAndSts).stream()
              .flatMap(node -> JsonNodeUtils.nodeToCollection(node.path("TxInfAndSts")).stream())
              .flatMap(node -> JsonNodeUtils.nodeToCollection(node.path("TxSts")).stream())
              .map(node -> node.asText(null))
              .map(StringUtils::trimToNull)
              .map(txtSt ->{
                  try {
                      return TransactionStatus.fromString(txtSt);
                  } catch (IllegalArgumentException e) {
                      return TransactionStatus.ERROR;
                  }
              })
              .collect(Collectors.toList());

      return transactionStatusList.isEmpty() ? List.of(TransactionStatus.ERROR) : transactionStatusList;
  }

  @NonNull
  public ResourceID exstractResourceIDSend(@NonNull final JsonNode responseNode) {

        final var orgnlMsgId = Optional.of(responseNode)
                .map(node -> node.path("AsynchronousSepaRequestToPayResponse"))
                .map(node -> node.path("Document"))
                .map(node -> node.path("CdtrPmtActvtnReqStsRpt"))
                .map(node -> node.path("OrgnlGrpInfAndSts"))
                .map(node -> node.path("OrgnlMsgId"))
                .filter(node -> !node.isMissingNode())
                .orElseThrow(() -> new IllegalArgumentException("Missing field"));

        return Optional.of(orgnlMsgId)
                .map(JsonNode::asText)
                .map(StringUtils::trimToNull)
                .map(IdentifierUtils::uuidRebuilder)
                .map(ResourceID::new)
                .orElseThrow(() -> new IllegalArgumentException("Resource id is invalid"));
    }
}
