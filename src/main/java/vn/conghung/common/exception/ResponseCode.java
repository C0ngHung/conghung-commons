package vn.conghung.common.exception;

public enum ResponseCode {

    COMMON_SUCCESS("0000", "Success"),

    REQ_VALIDATION_ERROR("1001", "Invalid input data"),
    REQ_BAD_REQUEST     ("1002", "Bad request"),
    REQ_MISSING_FIELD   ("1003", "Required field is missing"),
    REQ_INVALID_REQUEST ("1004", "Request is semantically invalid"),
    REQ_TYPE_MISMATCH   ("1005", "Field type mismatch"),

    AUTH_UNAUTHORIZED("2001", "Unauthorized"),

    PERM_FORBIDDEN("2101", "Forbidden"),

    DATA_NOT_FOUND("4001", "Resource not found"),
    DATA_CONFLICT  ("4002", "Conflict request"),

    SYS_INTERNAL_ERROR("9999", "Internal server error");

    private final String code;
    private final String defaultMessage;

    ResponseCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
