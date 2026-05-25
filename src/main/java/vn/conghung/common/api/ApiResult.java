package vn.conghung.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.conghung.common.exception.ResponseCode;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(ResultInfo result, T data, ErrorDetail error, OffsetDateTime requestDateTime) {

    private static final ZoneId ICT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS), data, null, OffsetDateTime.now(ICT_ZONE));
    }

    public static <T> ApiResult<T> ok(String description, T data) {
        return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS, description), data, null, OffsetDateTime.now(ICT_ZONE));
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode) {
        return new ApiResult<>(ResultInfo.of(responseCode), null, null, OffsetDateTime.now(ICT_ZONE));
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode, String description) {
        return new ApiResult<>(ResultInfo.of(responseCode, description), null, null, OffsetDateTime.now(ICT_ZONE));
    }

    public static <T> ApiResult<T> fail(ResponseCode responseCode, String description, Object details) {
        return new ApiResult<>(ResultInfo.of(responseCode, description), null, ErrorDetail.of(details), OffsetDateTime.now(ICT_ZONE));
    }
}
