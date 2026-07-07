package vn.conghung.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;
import vn.conghung.common.api.ApiResult;
import vn.conghung.common.api.ValidationError;

import java.util.List;

/**
 * Global exception handler for consuming services.
 *
 * <p>Ordered at {@link Ordered#LOWEST_PRECEDENCE} (TS-018 gap G4): because this advice contains a
 * catch-all {@code @ExceptionHandler(Exception.class)}, a service-specific {@code @RestControllerAdvice}
 * (unordered, thus higher precedence) is guaranteed to be consulted first. This removes the latent
 * risk of the generic catch-all shadowing a more specific handler declared elsewhere.
 */
@RestControllerAdvice
@Order
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String FIELD_REQUEST_BODY = "requestBody";

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ApiResult<Void>> handleTechnicalException(ApiException ex, HttpServletRequest request) {

        if (log.isErrorEnabled()) {
            log.error("Technical failure at {}: {}", sanitize(request.getRequestURI()), sanitize(ex.getMessage()), ex);
        }

        return createErrorResponse(ex.responseCode(), ex.userMessage(), ex.httpStatus());
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ApiResult<Void>> handleIntegrationException(IntegrationException ex, HttpServletRequest request) {

        if (log.isErrorEnabled()) {
            log.error("External integration failure at {}: extRef={}, corrId={}, message={}",
                    sanitize(request.getRequestURI()),
                    sanitize(ex.externalReference()),
                    sanitize(ex.correlationId()),
                    sanitize(ex.getMessage()),
                    ex);
        }

        return createErrorResponse(ex.responseCode(), ex.userMessage(), ex.httpStatus());
    }

    @ExceptionHandler(UnknownResultException.class)
    public ResponseEntity<ApiResult<Void>> handleUnknownResultException(UnknownResultException ex, HttpServletRequest request) {

        if (log.isErrorEnabled()) {

            log.error("Unknown transaction result at {}: txRef={}, extRef={}, corrId={}",
                    sanitize(request.getRequestURI()),
                    sanitize(ex.transactionReference()),
                    sanitize(ex.externalReference()),
                    sanitize(ex.correlationId()),
                    ex);
        }

        return createErrorResponse(ex.responseCode(), ex.userMessage(), ex.httpStatus());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResult<Void>> handleApiException(ApiException ex, HttpServletRequest request) {

        if (log.isWarnEnabled()) {

            log.warn("Business exception at {}: {}", sanitize(request.getRequestURI()), sanitize(ex.getMessage()));
        }

        return createErrorResponse(ex.responseCode(), ex.userMessage(), ex.httpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {

        if (log.isWarnEnabled()) {

            log.warn("Validation error at {}: {}", sanitize(request.getRequestURI()), sanitize(ex.getMessage()));
        }

        List<ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ValidationError(err.getField(), err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid value"))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResponseCode.REQ_VALIDATION_ERROR, ResponseCode.REQ_VALIDATION_ERROR.defaultMessage(), validationErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {

        if (log.isWarnEnabled()) {
            log.warn("Unreadable HTTP message at {}: {}", sanitize(request.getRequestURI()), sanitize(ex.getMessage()));
        }

        String fieldName = FIELD_REQUEST_BODY;
        String detailMessage;

        // Prefer structured Jackson causes over scraping the (version/locale-dependent) message text
        // — stable across Jackson/Spring upgrades and does not echo internal class paths (TS-018 gap G5).
        if (ex.getCause() instanceof MismatchedInputException mie) {
            fieldName = resolveFieldName(mie);
            detailMessage = describeMismatch(mie);
        } else {
            detailMessage = "Required request body is missing or malformed";
        }

        List<ValidationError> validationErrors = List.of(new ValidationError(fieldName, detailMessage));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResponseCode.REQ_BAD_REQUEST, ResponseCode.REQ_BAD_REQUEST.defaultMessage(), validationErrors));
    }

    /**
     * Builds a dotted field path from the Jackson reference chain (e.g. {@code user.age},
     * {@code items[2].price}). Falls back to {@code "requestBody"} when no path is available.
     */
    private String resolveFieldName(MismatchedInputException mie) {

        List<JacksonException.Reference> path = mie.getPath();

        if (path == null || path.isEmpty()) {
            return FIELD_REQUEST_BODY;
        }

        StringBuilder sb = new StringBuilder();

        for (JacksonException.Reference ref : path) {
            String property = ref.getPropertyName();
            if (property != null) {
                if (!sb.isEmpty()) {
                    sb.append('.');
                }
                sb.append(property);
            } else if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            }
        }

        return sb.isEmpty() ? FIELD_REQUEST_BODY : sb.toString();
    }

    /**
     * Describes a deserialization mismatch using the structured target type (and, for
     * {@link InvalidFormatException}, the offending value) instead of scraped message text.
     */
    private String describeMismatch(MismatchedInputException mie) {

        Class<?> targetType = mie.getTargetType();
        String type = targetType != null ? targetType.getSimpleName() : "";

        String value = "";
        if (mie instanceof InvalidFormatException ife && ife.getValue() != null) {
            value = String.valueOf(ife.getValue());
        }

        if (!type.isEmpty() && !value.isEmpty()) {
            return "Invalid value '" + value + "' for type " + type;
        }

        if (!type.isEmpty()) {
            return "Invalid format for type " + type;
        }

        return "Invalid value format";
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResult<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        if (log.isWarnEnabled()) {

            log.warn("Unsupported HTTP method '{}' at {}: {}", ex.getMethod(), sanitize(request.getRequestURI()), sanitize(ex.getMessage()));

        }
        return createErrorResponse(ResponseCode.REQ_INVALID_REQUEST, ResponseCode.REQ_INVALID_REQUEST.defaultMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        if (log.isWarnEnabled()) {

            log.warn("Type mismatch on parameter '{}' at {}: {}", ex.getName(), sanitize(request.getRequestURI()), sanitize(ex.getMessage()));

        }

        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

        String providedValue = ex.getValue() != null ? ex.getValue().toString() : "null";

        List<ValidationError> validationErrors = List.of(new ValidationError(ex.getName(), "Expected type: " + expectedType + ", provided value: '" + providedValue + "'"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResponseCode.REQ_TYPE_MISMATCH, ResponseCode.REQ_TYPE_MISMATCH.defaultMessage(), validationErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        if (log.isWarnEnabled()) {

            log.warn("Constraint violation at {}: {}", sanitize(request.getRequestURI()), sanitize(ex.getMessage()));

        }
        List<ValidationError> validationErrors = parseConstraintViolations(ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResponseCode.REQ_VALIDATION_ERROR, ResponseCode.REQ_VALIDATION_ERROR.defaultMessage(), validationErrors));
    }

    private List<ValidationError> parseConstraintViolations(ConstraintViolationException ex) {

        return ex.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                    return new ValidationError(fieldName, violation.getMessage() != null ? violation.getMessage() : "Invalid value");
                })
                .toList();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGenericException(Exception ex, HttpServletRequest request) {

        if (log.isErrorEnabled()) {
            log.error("Unhandled exception at {}: {}", sanitize(request.getRequestURI()), sanitize(ex.getMessage()), ex);
        }

        return createErrorResponse(ResponseCode.SYS_INTERNAL_ERROR, ResponseCode.SYS_INTERNAL_ERROR.defaultMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResult<Void>> createErrorResponse(ResponseCode code, String message, HttpStatus status) {

        return ResponseEntity.status(status).body(ApiResult.fail(code, message));
    }

    private @Nullable String sanitize(@Nullable String input) {
        if (input == null) {
            return null;
        }
        return input.replace('\n', '_').replace('\r', '_');
    }
}
