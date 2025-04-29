package it.gov.pagopa.rtp.activator.exception;

public enum ActivationErrorCode {
    // 400 Bad Request
    INVALID_REQUEST_FORMAT("01021000E", 400, "Invalid request format."),
    MISSING_REQUIRED_FIELD("01021001E", 400, "Missing required field."),
    INVALID_FISCAL_CODE_FORMAT("01021002E", 400, "Invalid fiscal code format."),
    INVALID_SERVICE_PROVIDER_ID_FORMAT("01021003E", 400, "Invalid RTP Service Provider ID format."),
    INVALID_REQUEST_PAYLOAD_STRUCTURE("01021004E", 400, "Invalid request payload structure."),
    INVALID_PAGE_PARAMETER("01021005E", 400, "Invalid page parameter."),
    INVALID_SIZE_PARAMETER("01021006E", 400, "Invalid size parameter."),
    INVALID_ACTIVATION_ID_FORMAT("01021007E", 400, "Invalid activation ID format."),
    MISSING_PAYER_ID("01021008E", 400, "Missing Payer ID."),
    REQUIRED_HEADER_MISSING("01021009E", 400, "Required header missing."),
    INVALID_PAYER_ID_FORMAT("01021013E", 400, "Invalid Payer ID format."),
    
    // 401 Unauthorized
    MISSING_AUTHENTICATION_TOKEN("01011000E", 401, "Missing authentication token."),
    INVALID_TOKEN_FORMAT("01011001E", 401, "Invalid token format."),
    EXPIRED_TOKEN("01011002E", 401, "Expired token."),
    INVALID_SIGNATURE("01011003F", 401, "Invalid signature."),
    
    // 403 Forbidden
    INSUFFICIENT_PERMISSIONS("01011004E", 403, "Insufficient permissions."),
    INVALID_SCOPE("01011005E", 403, "Invalid scope."),
    MISMATCH_SERVICE_PROVIDER_ID_TOKEN("01011006F", 403, "Mismatch between Payer's RTP Service Provider ID and token subject."),
    
    // 404 Not Found
    ACTIVATION_NOT_FOUND("01041000E", 404, "Activation not found."),
    ACTIVATION_NOT_VISIBLE("01041001E", 404, "Activation not visible to non-admin."),
    
    // 406 Not Acceptable
    UNSUPPORTED_ACCEPT_HEADER("01021010E", 406, "Unsupported accept header."),
    
    // 409 Conflict
    DUPLICATE_PAYER_ID_ACTIVATION("01031000E", 409, "Activation with the same Payer ID already exists."),
    DUPLICATE_ACTIVATION_REQUEST("01031001E", 409, "Duplicate activation request."),
    
    // 415 Unsupported Media Type
    UNSUPPORTED_CONTENT_TYPE("01021011E", 415, "Unsupported content type."),
    INVALID_CONTENT_ENCODING("01021012E", 415, "Invalid content encoding."),
    
    // 429 Too Many Requests
    RATE_LIMIT_EXCEEDED("01051000E", 429, "Rate limit exceeded."),
    QUOTA_EXCEEDED("01051001E", 429, "Quota exceeded."),
    
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("01091000F", 500, "Internal server error."),
    DATABASE_ERROR("01091001F", 500, "Database error.");
    
    private final String code;
    private final int httpStatus;
    private final String message;
    
    ActivationErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * Find error code by its string code
     */
    public static ActivationErrorCode findByCode(String code) {
        for (ActivationErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("No error code found for: " + code);
    }
}
