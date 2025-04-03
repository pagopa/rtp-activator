package it.gov.pagopa.rtp.activator.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Optional;

@Component
public class DateUtils {

    public static String LocalDateTimeToZulu(LocalDateTime localDateTime) {
        return Optional.of(localDateTime)
            .map(sdt-> sdt.toInstant(ZoneOffset.UTC))
            .map(sdt -> sdt.with(ChronoField.NANO_OF_SECOND, 0))
            .map(DateTimeFormatter.ISO_INSTANT::format)
            .orElseThrow(() -> new IllegalArgumentException("Couldn't convert saving datetime to Zulu format"));
    }

}
