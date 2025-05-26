package it.gov.pagopa.rtp.activator.domain.rtp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionStatusTest {

    @Test
    void givenValidStatusString_whenFromString_thenReturnCorrectEnum() {
        String input = "ACCP";

        TransactionStatus result = TransactionStatus.fromString(input);

        assertEquals(TransactionStatus.ACCP, result);
    }

    @Test
    void givenEmptyString_whenFromString_thenThrowIllegalArgumentException() {
        String input = "";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionStatus.fromString(input)
        );

        assertEquals("No matching Enum", exception.getMessage());
    }

    @Test
    void givenInvalidStatusString_whenFromString_thenThrowIllegalArgumentException() {
        String input = "INVALID";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionStatus.fromString(input)
        );

        assertEquals("No matching Enum", exception.getMessage());
    }

    @Test
    void givenNull_whenFromString_thenThrowIllegalArgumentException() {
        String input = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionStatus.fromString(input)
        );

        assertEquals("Input text must not be null", exception.getMessage());
    }

}
