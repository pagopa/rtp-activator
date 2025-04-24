package it.gov.pagopa.rtp.activator.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SendErrorCodeTest {

  @Test
  @DisplayName("findByCode should return correct enum for valid code")
  void findByCodeReturnsCorrectEnum() {
    // Given a valid error code
    String errorCode = "02021000F";

    // When findByCode is called
    SendErrorCode result = SendErrorCode.findByCode(errorCode);

    // Then it should return the correct enum
    assertEquals(SendErrorCode.INVALID_REQUEST_FORMAT, result);
    assertEquals(errorCode, result.getCode());
    assertEquals(400, result.getHttpStatus());
    assertEquals("Invalid request format.", result.getMessage());
  }

  @Test
  @DisplayName("findByCode should throw IllegalArgumentException for invalid code")
  void findByCodeThrowsExceptionForInvalidCode() {
    // Given an invalid error code
    String invalidCode = "99999999X";

    // When findByCode is called with invalid code, then it should throw
    // IllegalArgumentException
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> SendErrorCode.findByCode(invalidCode));

    // And the exception message should contain the invalid code
    assertTrue(exception.getMessage().contains(invalidCode));
  }

  @ParameterizedTest
  @EnumSource(SendErrorCode.class)
  @DisplayName("All enum values should have valid properties")
  void allEnumValuesHaveValidProperties(SendErrorCode errorCode) {
    // All enum values should have non-null properties
    assertNotNull(errorCode.getCode(), "Error code should not be null");
    assertNotNull(errorCode.getMessage(), "Error message should not be null");

    // All error codes should follow pattern (digits followed by 'F')
    assertTrue(errorCode.getCode().matches("\\d{8}[FEW]"),
        "Error code should match pattern of 8 digits followed by 'F', 'E', or 'W'");

    // HTTP status should be a valid HTTP status code
    int status = errorCode.getHttpStatus();
    assertTrue(status >= 100 && status < 600,
        "HTTP status should be between 100 and 599");
  }

  @Test
  @DisplayName("Check specific error codes have correct values")
  void specificErrorCodesHaveCorrectValues() {
    // 400 Bad Request
    assertEquals("02021000F", SendErrorCode.INVALID_REQUEST_FORMAT.getCode());
    assertEquals(400, SendErrorCode.INVALID_REQUEST_FORMAT.getHttpStatus());

    // 401 Unauthorized
    assertEquals("02011000F", SendErrorCode.MISSING_AUTHENTICATION_TOKEN.getCode());
    assertEquals(401, SendErrorCode.MISSING_AUTHENTICATION_TOKEN.getHttpStatus());

    // 500 Internal Server Error
    assertEquals("02091000F", SendErrorCode.INTERNAL_SERVER_ERROR.getCode());
    assertEquals(500, SendErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
  }

  @Test
  @DisplayName("Codes should be unique")
  void codesShouldBeUnique() {
    // Check that all error codes are unique
    long uniqueCodes = Arrays.stream(SendErrorCode.values())
        .map(SendErrorCode::getCode)
        .distinct()
        .count();

    assertEquals(SendErrorCode.values().length, uniqueCodes,
        "All error codes should be unique");
  }

  @ParameterizedTest
  @MethodSource("httpStatusTestCases")
  @DisplayName("Error codes with specific HTTP status should be correctly grouped")
  void errorCodesWithSpecificHttpStatus(int httpStatus, int expectedCount) {
    // Count error codes with the given HTTP status
    long count = Arrays.stream(SendErrorCode.values())
        .filter(code -> code.getHttpStatus() == httpStatus)
        .count();

    assertEquals(expectedCount, count,
        "Unexpected number of error codes with HTTP status " + httpStatus);
  }

  private static Stream<Arguments> httpStatusTestCases() {
    return Stream.of(
        Arguments.of(400, 11), // 11 error codes with status 400
        Arguments.of(401, 4), // 4 error codes with status 401
        Arguments.of(403, 3), // 3 error codes with status 403
        Arguments.of(500, 3) // 3 error codes with status 500
    );
  }
}