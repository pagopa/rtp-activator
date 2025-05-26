package it.gov.pagopa.rtp.activator.service.callback;


import com.fasterxml.jackson.databind.JsonNode;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.utils.IdentifierUtils;
import it.gov.pagopa.rtp.activator.utils.JsonNodeUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Component("callbackFieldsExtractor")
public class CallbackFieldsExtractor {

  @NonNull
  public Flux<TransactionStatus> extractTransactionStatusSend(@NonNull final JsonNode responseNode) {
      return Mono.justOrEmpty(responseNode)
              .doOnNext(node -> log.debug("Received JSON for transaction status extraction: {}", node))
              .map(node -> node.path("AsynchronousSepaRequestToPayResponse")
                      .path("Document")
                      .path("CdtrPmtActvtnReqStsRpt")
                      .path("OrgnlPmtInfAndSts"))
              .doOnNext(node -> log.debug("Navigated to OrgnlPmtInfAndSts node: {}", node))
              .filter(node -> !node.isMissingNode())
              .switchIfEmpty(Mono.error(new IllegalArgumentException("Missing field")))
              .flatMapMany(JsonNodeUtils::nodeToFlux)
              .flatMap(node -> JsonNodeUtils.nodeToFlux(node.path("TxInfAndSts")))
              .flatMap(node -> JsonNodeUtils.nodeToFlux(node.path("TxSts")))
              .map(JsonNode::asText)
              .map(StringUtils::trim)
              .doOnNext(txSt -> log.debug("Extracted raw transaction status: '{}'", txSt))
              .map(txtSt -> {
                  try {
                      TransactionStatus status = TransactionStatus.fromString(txtSt);
                      log.info("Mapped transaction status to enum: {}", status);
                      return status;
                  } catch (IllegalArgumentException e) {
                      log.warn("Invalid transaction status '{}', defaulting to ERROR", txtSt);
                      return TransactionStatus.ERROR;
                  }
              })
              .switchIfEmpty(Flux.just(TransactionStatus.ERROR));
  }

  @NonNull
  public Mono<ResourceID> extractResourceIDSend(@NonNull final JsonNode responseNode) {
      return Mono.justOrEmpty(responseNode)
              .doOnNext(node -> log.debug("Received JSON for resource ID extraction: {}", node))
              .map(node -> node.path("AsynchronousSepaRequestToPayResponse")
                      .path("Document")
                      .path("CdtrPmtActvtnReqStsRpt")
                      .path("OrgnlGrpInfAndSts")
                      .path("OrgnlMsgId"))
              .doOnNext(node -> log.debug("Navigated to OrgnlMsgId node: {}", node))
              .filter(node -> !node.isMissingNode())
              .switchIfEmpty(Mono.error(new IllegalArgumentException("Missing field")))
              .map(JsonNode::asText)
              .map(StringUtils::trim)
              .doOnNext(value -> log.debug("Extracted original message ID: '{}'", value))
              .map(IdentifierUtils::uuidRebuilder)
              .doOnNext(uuid -> log.debug("Rebuilt UUID: {}", uuid))
              .map(ResourceID::new)
              .doOnNext(id -> log.info("Extracted ResourceID: {}", id))
              .switchIfEmpty(Mono.error(new IllegalArgumentException("Resource id is invalid")));
  }
}
