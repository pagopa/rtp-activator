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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Component("callbackFieldsExtractor")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CallbackFieldsExtractor {

  @NonNull
  public Flux<TransactionStatus> extractTransactionStatusSend(@NonNull final JsonNode responseNode) {
      return Mono.justOrEmpty(responseNode)
              .map(node -> node.path("AsynchronousSepaRequestToPayResponse")
                      .path("Document")
                      .path("CdtrPmtActvtnReqStsRpt")
                      .path("OrgnlPmtInfAndSts"))
              .filter(node -> !node.isMissingNode())
              .switchIfEmpty(Mono.error(new IllegalArgumentException("Missing field")))
              .flatMapMany(JsonNodeUtils::nodeToFlux)
              .flatMap(node -> JsonNodeUtils.nodeToFlux(node.path("TxInfAndSts")))
              .flatMap(node -> JsonNodeUtils.nodeToFlux(node.path("TxSts")))
              .map(JsonNode::asText)
              .map(StringUtils::trim)
              .map(txtSt -> {
                  try {
                      return TransactionStatus.fromString(txtSt);
                  } catch (IllegalArgumentException e) {
                      return TransactionStatus.ERROR;
                  }
              })
              .switchIfEmpty(Flux.just(TransactionStatus.ERROR));
  }

  @NonNull
  public Mono<ResourceID> exstractResourceIDSend(@NonNull final JsonNode responseNode) {

      return Mono.justOrEmpty(responseNode)
              .map(node -> node.path("AsynchronousSepaRequestToPayResponse")
                      .path("Document")
                      .path("CdtrPmtActvtnReqStsRpt")
                      .path("OrgnlGrpInfAndSts")
                      .path("OrgnlMsgId"))
              .filter(node -> !node.isMissingNode())
              .switchIfEmpty(Mono.error(new IllegalArgumentException("Missing field")))
              .map(JsonNode::asText)
              .map(StringUtils::trim)
              .map(IdentifierUtils::uuidRebuilder)
              .map(ResourceID::new)
              .switchIfEmpty(Mono.error(new IllegalArgumentException("Resource id is invalid")));
  }
}
