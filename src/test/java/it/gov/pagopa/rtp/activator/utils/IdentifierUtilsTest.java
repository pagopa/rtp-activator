package it.gov.pagopa.rtp.activator.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdentifierUtilsTest {

    @Test
    void givenValidUuid_whenUuidFormatter_thenFormattedUuidWithoutHyphens() {
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        String result = IdentifierUtils.uuidFormatter(uuid);

        assertEquals("123e4567e89b12d3a456426614174000", result);
    }

    @Test
    void givenNullUuid_whenUuidFormatter_thenThrowNullPointerException() {
        UUID uuid = null;

        assertThrows(NullPointerException.class, () -> IdentifierUtils.uuidFormatter(uuid));
    }

    @Test
    void givenEmptyString_whenUuidFormatter_thenThrowIllegalArgumentException() {
        String emptyString = "";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> IdentifierUtils.uuidFormatter(UUID.fromString(emptyString))
        );

        assertEquals("Invalid UUID string: ", thrown.getMessage());
    }

    @Test
    void givenValidUuidWithoutDashes_whenUuidRebuilder_thenReturnsProperUuid() {
        String uuidWithoutDashes = "123e4567e89b12d3a456426614174000";
        String expectedFormatted = "123e4567-e89b-12d3-a456-426614174000";
        UUID expectedUuid = UUID.fromString(expectedFormatted);

        UUID result = IdentifierUtils.uuidRebuilder(uuidWithoutDashes);

        assertEquals(expectedUuid, result);
    }

    @Test
    void givenUuidWithDashes_whenUuidRebuilder_thenReturnsSameUuid() {
        String uuidWithDashes = "123e4567-e89b-12d3-a456-426614174000";
        UUID expectedUuid = UUID.fromString(uuidWithDashes);

        UUID result = IdentifierUtils.uuidRebuilder(uuidWithDashes);

        assertEquals(expectedUuid, result);
    }

    @Test
    void givenInvalidUuid_whenUuidRebuilder_thenThrowsIllegalArgumentException() {
        String invalidUuid = "invalid-uuid-string";

        assertThrows(IllegalArgumentException.class, () -> IdentifierUtils.uuidRebuilder(invalidUuid));
    }

    @Test
    void givenNullUuidString_whenUuidRebuilder_thenThrowsNullPointerException() {
        String nullUuid = null;

        assertThrows(NullPointerException.class, () -> IdentifierUtils.uuidRebuilder(nullUuid));
    }

    @Test
    void givenValidUuidWithoutDashes_whenIsValidUuid_thenReturnsTrue() {
        String input = "123e4567e89b12d3a456426614174000";
        assertTrue(IdentifierUtils.isValidUuid(input));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "123e4567-e89b-12d3-a456-426614174000",
            " ",
            "123e4567e89b12d3a45642661417zzzz"
    })
    void givenInvalidUuidInputs_whenIsValidUuid_thenReturnsFalse(String input) {
        assertFalse(IdentifierUtils.isValidUuid(input));
    }
}
