package vn.conghung.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.conghung.common.exception.ResponseCode;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(String code, String message, T data, ErrorDetail error, String traceId, Instant timestamp) {

    public static <T> ApiResult<T> ok(T data, String traceId) {
        return ok(ResponseCode.COMMON_SUCCESS.defaultMessage(), data, traceId);
    }

    public static <T> ApiResult<T> ok(String message, T data, String traceId) {
        return new ApiResult<>(ResponseCode.COMMON_SUCCESS.code(), message, data, null, traceId, Instant.now());
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode, String traceId) {
        return new ApiResult<>(responseCode.code(), responseCode.defaultMessage(), null, ErrorDetail.of(responseCode), traceId, Instant.now());
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode, String message, String traceId) {
        return new ApiResult<>(responseCode.code(), message, null, ErrorDetail.of(responseCode, message), traceId, Instant.now());
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode, String message, Object details, String traceId) {
        return new ApiResult<>(responseCode.code(), message, null, ErrorDetail.of(responseCode, details), traceId, Instant.now());
    }
}
