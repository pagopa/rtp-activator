package it.gov.pagopa.rtp.activator.domain.rtp;

public enum RtpStatus {
  CREATED,
  SENT,
  CANCELLED,
  ACCEPTED,
  REJECTED,
  USER_ACCEPTED,
  USER_REJECTED,
  PAYED,
  ERROR_SEND,
  CANCELLED_ACCR,
  CANCELLED_REJECTED,
  ERROR_CANCEL
}
