package vn.conghung.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.conghung.common.api.ApiResult;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TRACE_ID_KEY = "traceId";

    @ExceptionHandler({TechnicalException.class, IntegrationException.class})
    public ResponseEntity<ApiResult<Void>> handleInfrastructureException(ApiException ex, HttpServletRequest request) {

        log.error("Infrastructure/Integration failure at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return createErrorResponse(ex.responseCode(), ex.userMessage(), ex.httpStatus());
    }

    @ExceptionHandler(UnknownResultException.class)
    public ResponseEntity<ApiResult<Void>> handleUnknownResultException(UnknownResultException ex, HttpServletRequest request) {

        log.error("Unknown transaction result at {}: txRef={}, extRef={}, corrId={}",
                request.getRequestURI(), ex.transactionReference(), ex.externalReference(), ex.correlationId(), ex);

        return createErrorResponse(ex.responseCode(), ex.userMessage(), ex.httpStatus());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResult<Void>> handleApiException(ApiException ex, HttpServletRequest request) {

        log.warn("Business exception at {}: {}", request.getRequestURI(), ex.getMessage());

        return createErrorResponse(ex.responseCode(), ex.userMessage(), ex.httpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation error at {}: {}", request.getRequestURI(), ex.getMessage());

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResponseCode.REQ_VALIDATION_ERROR, ResponseCode.REQ_VALIDATION_ERROR.defaultMessage(), errors, getTraceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGenericException(Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return createErrorResponse(ResponseCode.SYS_INTERNAL_ERROR, ResponseCode.SYS_INTERNAL_ERROR.defaultMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResult<Void>> createErrorResponse(ResponseCode code, String message, HttpStatus status) {

        return ResponseEntity.status(status).body(ApiResult.fail(code, message, getTraceId()));
    }

    private String getTraceId() {
        return MDC.get(TRACE_ID_KEY) != null ? MDC.get(TRACE_ID_KEY) : "unknown";
    }
}
