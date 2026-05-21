package vn.conghung.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.conghung.common.exception.ResponseCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(String code, String message, Object details) {

    public static ErrorDetail of(ResponseCode responseCode) {
        return new ErrorDetail(responseCode.code(), responseCode.defaultMessage(), null);
    }

    public static ErrorDetail of(ResponseCode responseCode, String message) {
        return new ErrorDetail(responseCode.code(), message, null);
    }

    public static ErrorDetail of(ResponseCode responseCode, Object details) {
        return new ErrorDetail(responseCode.code(), responseCode.defaultMessage(), details);
    }
}
