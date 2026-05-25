package vn.conghung.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.conghung.common.exception.ResponseCode;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(ResultInfo result, T data, ErrorDetail error, Instant timestamp) {

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS), data, null, Instant.now());
    }

    public static <T> ApiResult<T> ok(String description, T data) {
        return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS, description), data, null, Instant.now());
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode) {
        return new ApiResult<>(ResultInfo.of(responseCode), null, null, Instant.now());
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode, String description) {
        return new ApiResult<>(ResultInfo.of(responseCode, description), null, null, Instant.now());
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode, String description, Object details) {
        return new ApiResult<>(ResultInfo.of(responseCode, description), null, ErrorDetail.of(details), Instant.now());
    }
}
