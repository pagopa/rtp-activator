package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.List;
import java.util.function.Consumer;


public class RtpTransition extends Transition<RtpEntity, RtpStatus, RtpEvent> {


  public RtpTransition(
      RtpStatus source, RtpEvent event, RtpStatus destination,
      List<Consumer<RtpEntity>> preTransactionActions,
      List<Consumer<RtpEntity>> postTransactionActions) {

    super(source, event, destination, preTransactionActions, postTransactionActions);
  }
}
