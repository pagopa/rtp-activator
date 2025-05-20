package it.gov.pagopa.rtp.activator.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void givenValidLocalDateTime_whenConvertedToOffsetFormat_thenReturnsCorrectString() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 3, 25, 10, 15, 30);
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.of("Europe/Rome")).withNano(0);
        String expected = zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String result = DateUtils.localDateTimeToOffsetFormat(localDateTime);
        assertEquals(expected, result);
    }

    @Test
    void givenLocalDateTimeWithNanos_whenConvertedToOffsetFormat_thenTruncatesToMilliseconds() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 3, 25, 10, 15, 30, 999_999_999);
        String result = DateUtils.localDateTimeToOffsetFormat(localDateTime);
        assertTrue(result.endsWith("+01:00") || result.endsWith("+02:00"));
        String regex = ".*T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?([+-]\\d{2}:\\d{2})";
        assertTrue(Pattern.matches(regex, result), "Result has more than milliseconds precision: " + result);
    }

    @Test
    void givenNullLocalDateTime_whenConvertedToOffsetFormat_thenThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> DateUtils.localDateTimeToOffsetFormat(null));
        assertEquals("Couldn't convert local datetime to offset format", exception.getMessage());
    }

    @Test
    void givenDateBeforeDSTStart_whenConverted_thenHasStandardOffset() {
        LocalDateTime beforeDST = LocalDateTime.of(2024, 3, 24, 1, 0, 0);
        String result = DateUtils.localDateTimeToOffsetFormat(beforeDST);

        assertTrue(result.endsWith("+01:00"), "Expected +01:00 before DST start");
    }

    @Test
    void givenDateAfterDSTStart_whenConverted_thenHasDaylightOffset() {
        LocalDateTime afterDST = LocalDateTime.of(2024, 3, 31, 3, 0, 0);
        String result = DateUtils.localDateTimeToOffsetFormat(afterDST);

        assertTrue(result.endsWith("+02:00"), "Expected +02:00 after DST start");
    }

    @Test
    void givenDateAfterDSTEnd_whenConverted_thenHasStandardOffset() {
        LocalDateTime afterDSTEnd = LocalDateTime.of(2024, 10, 27, 4, 0, 0);
        String result = DateUtils.localDateTimeToOffsetFormat(afterDSTEnd);

        assertTrue(result.endsWith("+01:00"), "Expected +01:00 after DST ends");
    }
}
