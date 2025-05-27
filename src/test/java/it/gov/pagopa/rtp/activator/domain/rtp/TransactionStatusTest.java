package it.gov.pagopa.rtp.activator.domain.rtp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TransactionStatusTest {

    @Test
    void givenValidStatusString_whenFromString_thenReturnCorrectEnum() {
        String input = "ACCP";

        TransactionStatus result = TransactionStatus.fromString(input);

        assertEquals(TransactionStatus.ACCP, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "INVALID"})
    void givenInvalidStringInput_whenFromString_thenThrowsIllegalArgumentException(String input) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionStatus.fromString(input)
        );
        assertEquals("No matching Enum", exception.getMessage());
    }

    @Test
    void givenNullInput_whenFromString_thenThrowsIllegalArgumentException() {
        String input = null;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionStatus.fromString(input)
        );
        assertEquals("Input text must not be null", exception.getMessage());
    }

}
