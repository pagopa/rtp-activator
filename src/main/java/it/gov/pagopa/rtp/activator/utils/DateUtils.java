package it.gov.pagopa.rtp.activator.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DateUtils {

  public static String localDateTimeToOffsetFormat(LocalDateTime localDateTime) {
    return Optional.ofNullable(localDateTime)
        .map(ldt -> ldt.atZone(ZoneId.of("Europe/Rome")))
        .map(zdt -> zdt.withNano(0))
        .map(DateTimeFormatter.ISO_OFFSET_DATE_TIME::format)
        .orElseThrow(
            () -> new IllegalArgumentException("Couldn't convert local datetime to offset format"));
  }
}
