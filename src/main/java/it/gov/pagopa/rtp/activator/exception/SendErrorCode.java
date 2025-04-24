package it.gov.pagopa.rtp.activator.exception;

public enum SendErrorCode {
    // 400 Bad Request
    INVALID_REQUEST_FORMAT("02021000F", 400, "Invalid request format."),
    MISSING_REQUIRED_FIELD("02021001F", 400, "Missing required field."),
    INVALID_PAYEE_ID_FORMAT("02021002F", 400, "Invalid payeeId format."),
    INVALID_PAYER_ID_FORMAT("02021003F", 400, "Invalid payerId format."),
    INVALID_NOTICE_NUMBER_FORMAT("02021004F", 400, "Invalid noticeNumber format."),
    INVALID_AMOUNT_VALUE("02021005F", 400, "Invalid amount value."),
    INVALID_DESCRIPTION_FORMAT("02021006F", 400, "Invalid description format."),
    INVALID_SUBJECT_FORMAT("02021007F", 400, "Invalid subject format."),
    INVALID_EXPIRY_DATE_FORMAT("02021008F", 400, "Invalid expiryDate format."),
    REQUIRED_HEADER_MISSING("02021009F", 400, "Required header missing."),
    INVALID_RTP_ID_FORMAT("02021013F", 400, "Invalid rtpId format."),
    
    // 401 Unauthorized
    MISSING_AUTHENTICATION_TOKEN("02011000F", 401, "Missing authentication token."),
    INVALID_TOKEN_FORMAT("02011001F", 401, "Invalid token format."),
    EXPIRED_TOKEN("02011002F", 401, "Expired token."),
    INVALID_SIGNATURE("02011003F", 401, "Invalid signature."),
    
    // 403 Forbidden
    INSUFFICIENT_PERMISSIONS("02011004F", 403, "Insufficient permissions."),
    INVALID_SCOPE("02011005F", 403, "Invalid scope."),
    ACCOUNT_SUSPENDED("02011006F", 403, "Account suspended."),
    
    // 404 Not Found
    RTP_NOT_FOUND("02041000F", 404, "RTP not found."),
    
    // 406 Not Acceptable
    UNSUPPORTED_ACCEPT_HEADER("02021010F", 406, "Unsupported accept header."),
    
    // 409 Conflict
    NOTICE_ALREADY_PROCESSED("02031000F", 409, "Notice already processed."),
    DUPLICATE_REQUEST("02031001F", 409, "Duplicate request."),
    
    // 415 Unsupported Media Type
    UNSUPPORTED_CONTENT_TYPE("02021011F", 415, "Unsupported content type."),
    INVALID_CONTENT_ENCODING("02021012F", 415, "Invalid content encoding."),
    
    // 422 Unprocessable Entity
    SERVICE_PROVIDER_REJECTION("02031002F", 422, "Service Provider rejection."),
    INVALID_PAYMENT_NOTICE("02031003F", 422, "Invalid payment notice."),
    EXPIRED_PAYMENT_NOTICE("02031004F", 422, "Expired payment notice."),
    BUSINESS_RULE_VIOLATION("02031005F", 422, "Business rule violation."),
    
    // 429 Too Many Requests
    RATE_LIMIT_EXCEEDED("02051000F", 429, "Rate limit exceeded."),
    QUOTA_EXCEEDED("02051001F", 429, "Quota exceeded."),
    
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("02091000F", 500, "Internal server error."),
    DATABASE_ERROR("02091001F", 500, "Database error."),
    INTEGRATION_FAILURE("02091002F", 500, "Integration failure."),
    
    // 504 Gateway Timeout
    DEBTOR_SERVICE_PROVIDER_TIMEOUT("02051002F", 504, "Debtor Service Provider timeout."),
    INTEGRATION_PROCESSING_TIMEOUT("02051003F", 504, "Integration processing timeout.");
    
    private final String code;
    private final int httpStatus;
    private final String message;
    
    SendErrorCode(String code, int httpStatus, String message) {
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
    public static SendErrorCode findByCode(String code) {
        for (SendErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("No error code found for: " + code);
    }
}
