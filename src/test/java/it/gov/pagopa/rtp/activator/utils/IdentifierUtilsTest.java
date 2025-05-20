package it.gov.pagopa.rtp.activator.utils;

import org.junit.jupiter.api.Test;

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
    void givenNullUuid_whenUuidFormatter_thenThrowIllegalArgumentException() {
        UUID uuid = null;

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> IdentifierUtils.uuidFormatter(uuid));
        assertEquals("uuid cannot be null", thrown.getMessage());
    }

    @Test
    void givenEmptyUuid_whenUuidFormatter_thenFormattedEmptyString() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        String result = IdentifierUtils.uuidFormatter(uuid);

        assertEquals("00000000000000000000000000000000", result);
    }
}
