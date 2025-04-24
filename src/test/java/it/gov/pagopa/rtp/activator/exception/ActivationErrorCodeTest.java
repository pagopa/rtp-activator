package it.gov.pagopa.rtp.activator.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ActivationErrorCodeTest {

  @Test
  @DisplayName("findByCode should return correct enum for valid code")
  void findByCodeReturnsCorrectEnum() {
    // Given a valid error code
    String errorCode = "01021000F";

    // When findByCode is called
    ActivationErrorCode result = ActivationErrorCode.findByCode(errorCode);

    // Then it should return the correct enum
    assertEquals(ActivationErrorCode.INVALID_REQUEST_FORMAT, result);
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
        () -> ActivationErrorCode.findByCode(invalidCode));

    // And the exception message should contain the invalid code
    assertTrue(exception.getMessage().contains(invalidCode));
  }

  @ParameterizedTest
  @EnumSource(ActivationErrorCode.class)
  @DisplayName("All enum values should have valid properties")
  void allEnumValuesHaveValidProperties(ActivationErrorCode errorCode) {
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
    assertEquals("01021000F", ActivationErrorCode.INVALID_REQUEST_FORMAT.getCode());
    assertEquals(400, ActivationErrorCode.INVALID_REQUEST_FORMAT.getHttpStatus());

    // 401 Unauthorized
    assertEquals("01011000F", ActivationErrorCode.MISSING_AUTHENTICATION_TOKEN.getCode());
    assertEquals(401, ActivationErrorCode.MISSING_AUTHENTICATION_TOKEN.getHttpStatus());

    // 404 Not Found
    assertEquals("01041000F", ActivationErrorCode.ACTIVATION_NOT_FOUND.getCode());
    assertEquals(404, ActivationErrorCode.ACTIVATION_NOT_FOUND.getHttpStatus());

    // 500 Internal Server Error
    assertEquals("01091000F", ActivationErrorCode.INTERNAL_SERVER_ERROR.getCode());
    assertEquals(500, ActivationErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
  }

  @Test
  @DisplayName("Codes should be unique")
  void codesShouldBeUnique() {
    // Check that all error codes are unique
    long uniqueCodes = Arrays.stream(ActivationErrorCode.values())
        .map(ActivationErrorCode::getCode)
        .distinct()
        .count();

    assertEquals(ActivationErrorCode.values().length, uniqueCodes,
        "All error codes should be unique");
  }

  @ParameterizedTest
  @MethodSource("httpStatusTestCases")
  @DisplayName("Error codes with specific HTTP status should be correctly grouped")
  void errorCodesWithSpecificHttpStatus(int httpStatus, int expectedCount) {
    // Count error codes with the given HTTP status
    long count = Arrays.stream(ActivationErrorCode.values())
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
        Arguments.of(404, 2), // 2 error codes with status 404
        Arguments.of(406, 1), // 1 error code with status 406
        Arguments.of(409, 2), // 2 error codes with status 409
        Arguments.of(415, 2), // 2 error codes with status 415
        Arguments.of(429, 2), // 2 error codes with status 429
        Arguments.of(500, 2) // 2 error codes with status 500
    );
  }
}