package vn.conghung.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.conghung.common.exception.ResponseCode;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(ResultInfo result, T data, ErrorDetail error, OffsetDateTime requestDateTime) {

    private static final ZoneId ICT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Success response with data.
     *
     * @param data response payload
     * @return ApiResult with COMMON_SUCCESS code and the given data
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS), data, null, OffsetDateTime.now(ICT_ZONE));
    }

    /**
     * Success response with a custom description and data.
     *
     * @param description custom success message
     * @param data        response payload
     * @return ApiResult with COMMON_SUCCESS code, custom description, and the given data
     */
    public static <T> ApiResult<T> ok(String description, T data) {
        return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS, description), data, null, OffsetDateTime.now(ICT_ZONE));
    }

    /**
     * Success response for void operations that return no data (delete, logout, soft-delete, etc.).
     *
     * <p>Prefer this over {@code ApiResult.ok(null)} for clarity.
     *
     * <p>Usage: {@code return ResponseEntity.ok(ApiResult.ok());}
     *
     * @return ApiResult with COMMON_SUCCESS code and null data
     */
    public static ApiResult<Void> ok() {
        return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS), null, null, OffsetDateTime.now(ICT_ZONE));
    }

    /**
     * Success response for void operations with a custom description message.
     *
     * <p><b>Naming note:</b> This method is named {@code noData} — not {@code ok(String)} —
     * to avoid compile-time ambiguity with {@code ok(T data)} when {@code T = String}.
     *
     * <p>Usage: {@code return ResponseEntity.ok(ApiResult.noData("User deleted successfully"));}
     *
     * @param description custom success message
     * @return ApiResult with COMMON_SUCCESS code, custom description, and null data
     */
    public static ApiResult<Void> noData(String description) {
        return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS, description), null, null, OffsetDateTime.now(ICT_ZONE));
    }

    /**
     * Error response.
     *
     * @param responseCode error code
     * @return ApiResult with the given error code and null data
     */
    public static <T> ApiResult<T> fail(ResponseCode responseCode) {
        return new ApiResult<>(ResultInfo.of(responseCode), null, null, OffsetDateTime.now(ICT_ZONE));
    }

    /**
     * Error response with a custom description.
     *
     * @param responseCode error code
     * @param description  custom error message
     * @return ApiResult with the given error code and custom description
     */
    public static <T> ApiResult<T> fail(ResponseCode responseCode, String description) {
        return new ApiResult<>(ResultInfo.of(responseCode, description), null, null, OffsetDateTime.now(ICT_ZONE));
    }

    /**
     * Error response with details (e.g. validation errors).
     *
     * @param responseCode error code
     * @param description  custom error message
     * @param details      structured error details (field errors, etc.)
     * @return ApiResult with error code, description, and error detail object
     */
    public static <T> ApiResult<T> fail(ResponseCode responseCode, String description, Object details) {
        return new ApiResult<>(ResultInfo.of(responseCode, description), null, ErrorDetail.of(details), OffsetDateTime.now(ICT_ZONE));
    }
}
