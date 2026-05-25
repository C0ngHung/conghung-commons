package vn.conghung.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import vn.conghung.common.api.ApiResult;
import vn.conghung.common.api.ValidationError;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test-uri");
    }

    @Test
    void testHandleTechnicalException() {
        TechnicalException ex = new TechnicalException(
                ResponseCode.SYS_INTERNAL_ERROR,
                "Technical error occurred",
                new RuntimeException("database failure")
        );

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleTechnicalException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("9999", response.getBody().result().responseCode());
        assertEquals("Technical error occurred", response.getBody().result().description());
    }

    @Test
    void testHandleIntegrationException() {
        IntegrationException ex = new IntegrationException(
                ResponseCode.SYS_INTERNAL_ERROR,
                "Partner system error",
                "EXT-12345",
                "CORR-56789",
                new RuntimeException("timeout")
        );

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleIntegrationException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("9999", response.getBody().result().responseCode());
        assertEquals("Partner system error", response.getBody().result().description());
    }

    @Test
    void testHandleUnknownResultException() {
        UnknownResultException ex = new UnknownResultException(
                ResponseCode.SYS_INTERNAL_ERROR,
                "TX-999",
                "EXT-999",
                "CORR-999",
                new RuntimeException("read timeout")
        );

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleUnknownResultException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("9999", response.getBody().result().responseCode());
    }

    @Test
    void testHandleApiException() {
        BusinessException ex = new BusinessException(ResponseCode.REQ_VALIDATION_ERROR, "Validation error occurred");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleApiException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1001", response.getBody().result().responseCode());
        assertEquals("Validation error occurred", response.getBody().result().description());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("form", "amount", "must not be null");
        FieldError fieldError2 = new FieldError("form", "currency", "must not be blank");
        FieldError fieldError3 = new FieldError("form", "email", null); // triggers default message logic

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2, fieldError3));
        when(ex.getMessage()).thenReturn("Validation context details");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleValidationException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1001", response.getBody().result().responseCode());
        
        assertNotNull(response.getBody().error());
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertNotNull(details);
        assertEquals(3, details.size());
        assertTrue(details.contains(new ValidationError("amount", "must not be null")));
        assertTrue(details.contains(new ValidationError("currency", "must not be blank")));
        assertTrue(details.contains(new ValidationError("email", "Invalid value")));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected server crash");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleGenericException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("9999", response.getBody().result().responseCode());
        assertEquals("Internal server error", response.getBody().result().description());
    }

    @Test
    void testSanitizeNullInput() {
        // We trigger Exception test with null to verify null-safety coverage of sanitize methods if any
        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleGenericException(new RuntimeException((String) null), request);
        assertNotNull(response);
    }
}
