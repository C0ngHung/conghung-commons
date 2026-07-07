package vn.conghung.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Wrapper for structured error details attached to an {@link ApiResult} error response.
 *
 * <p>{@code details} is intentionally typed as {@link Object} to stay open for different error
 * shapes. In practice it is one of:
 * <ul>
 *   <li>{@code List<}{@link ValidationError}{@code >} — field-level validation / binding errors
 *       (the common case, produced by the global exception handler);</li>
 *   <li>a service-specific POJO/map describing a domain error.</li>
 * </ul>
 * Consumers should treat it as an opaque, JSON-serializable payload and branch on the accompanying
 * {@code responseCode} rather than on the concrete Java type.
 *
 * @param details structured, JSON-serializable error payload (see shapes above)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(Object details) {

    public static ErrorDetail of(Object details) {
        return new ErrorDetail(details);
    }
}
