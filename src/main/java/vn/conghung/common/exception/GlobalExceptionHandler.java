package vn.conghung.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.conghung.common.api.ApiResult;
import vn.conghung.common.api.ValidationError;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ApiResult<Void>> handleTechnicalException(ApiException ex, HttpServletRequest request) {

        if (log.isErrorEnabled()) {log.error("Technical failure at {}: {}", sanitize(request.getRequestURI()), sanitize(ex.getMessage()), ex);
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
            log.warn("Business exception at {}: {}",
                    sanitize(request.getRequestURI()),
                    sanitize(ex.getMessage()));
        }

        return createErrorResponse(ex.responseCode(), ex.userMessage(), ex.httpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {

        if (log.isWarnEnabled()) {
            log.warn("Validation error at {}: {}",
                    sanitize(request.getRequestURI()),
                    sanitize(ex.getMessage()));
        }


        List<ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ValidationError(
                        err.getField(),
                        err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid value"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResponseCode.REQ_VALIDATION_ERROR, ResponseCode.REQ_VALIDATION_ERROR.defaultMessage(), validationErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGenericException(Exception ex, HttpServletRequest request) {

        if (log.isErrorEnabled()) {
            log.error("Unhandled exception at {}: {}",
                    sanitize(request.getRequestURI()),
                    sanitize(ex.getMessage()),
                    ex);
        }

        return createErrorResponse(ResponseCode.SYS_INTERNAL_ERROR, ResponseCode.SYS_INTERNAL_ERROR.defaultMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResult<Void>> createErrorResponse(ResponseCode code, String message, HttpStatus status) {

        return ResponseEntity.status(status).body(ApiResult.fail(code, message));
    }

    private String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replace('\n', '_').replace('\r', '_');
    }
}
