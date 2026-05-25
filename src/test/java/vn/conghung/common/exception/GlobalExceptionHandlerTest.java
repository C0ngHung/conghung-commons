package vn.conghung.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import vn.conghung.common.api.ApiResult;
import vn.conghung.common.api.ValidationError;

import java.util.List;
import java.util.Set;

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
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Invalid JSON structure");
        
        // Test with null specific cause message
        Throwable mockCauseWithNullMsg = mock(Throwable.class);
        when(mockCauseWithNullMsg.getMessage()).thenReturn(null);
        when(ex.getMostSpecificCause()).thenReturn(mockCauseWithNullMsg);
        
        ResponseEntity<ApiResult<Void>> responseNullCause = exceptionHandler.handleHttpMessageNotReadableException(ex, request);
        assertNotNull(responseNullCause);
        assertEquals(HttpStatus.BAD_REQUEST, responseNullCause.getStatusCode());
        assertEquals("1002", responseNullCause.getBody().result().responseCode());
        List<ValidationError> detailsNull = (List<ValidationError>) responseNullCause.getBody().error().details();
        assertEquals("Required request body is missing or malformed", detailsNull.get(0).message());

        // Test with specific cause containing colon
        Throwable causeWithColon = new RuntimeException("JSON parsing failed: Cannot deserialize value of type java.lang.Integer from String");
        when(ex.getMostSpecificCause()).thenReturn(causeWithColon);
        ResponseEntity<ApiResult<Void>> responseWithColon = exceptionHandler.handleHttpMessageNotReadableException(ex, request);
        assertNotNull(responseWithColon);
        List<ValidationError> detailsWithColon = (List<ValidationError>) responseWithColon.getBody().error().details();
        assertEquals("Cannot deserialize value of type java.lang.Integer from String", detailsWithColon.get(0).message());

        // Test with specific cause without colon
        Throwable causeWithoutColon = new RuntimeException("Malformed UTF-8 characters");
        when(ex.getMostSpecificCause()).thenReturn(causeWithoutColon);
        ResponseEntity<ApiResult<Void>> responseWithoutColon = exceptionHandler.handleHttpMessageNotReadableException(ex, request);
        assertNotNull(responseWithoutColon);
        List<ValidationError> detailsWithoutColon = (List<ValidationError>) responseWithoutColon.getBody().error().details();
        assertEquals("Malformed UTF-8 characters", detailsWithoutColon.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_MissingBody() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Required request body is missing: public void createUser(UserDto)");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);
        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("requestBody", details.get(0).field());
        assertEquals("Required request body is missing", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_JacksonFieldExtraction() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Cannot deserialize value of type 'java.lang.Integer' from String \"abc\": not a valid Integer value through reference chain: vn.conghung.dto.UserDto[\"age\"]");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);
        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("age", details.get(0).field());
        assertEquals("Invalid value 'abc' for type Integer", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_JacksonRedactedSensitiveDetails() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION internal class path leaks vn.conghung.dto.UserDto[\"email\"]");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);
        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("requestBody", details.get(0).field());
        assertEquals("Malformed JSON request body", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_NullMessage() {
        // Covers: resolveDetailMessage -> msg == null branch
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn(null);

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("requestBody", details.get(0).field());
        assertEquals("Required request body is missing or malformed", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_NoReferenceChainQuotes() {
        // Covers: 'through reference chain:' present but quotes/brackets malformed -> fieldName stays "requestBody"
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Cannot deserialize value of type 'java.lang.Integer' from String \"abc\": not a valid Integer value through reference chain: vn.conghung.dto.UserDto[age]");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        // field remains "requestBody" because the ["..."] pattern is absent
        assertEquals("requestBody", details.get(0).field());
        assertEquals("Invalid value 'abc' for type Integer", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_MostSpecificCauseRedacted() {
        // Covers: msg is clean, but mostSpecificCause contains sensitive StreamReadFeature -> REDACTED branch in resolveDetailMessage
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Some generic parse error");
        Throwable sensitiveCause = new RuntimeException("StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION internal classpath leak");
        when(ex.getMostSpecificCause()).thenReturn(sensitiveCause);

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("Malformed JSON request body", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_MismatchedValueSingleQuotes() {
        // Covers: parseMismatchedInput -> extractValueFromString with single-quote fallback branch
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Cannot deserialize value of type 'java.lang.Long' from String 'xyz': not a valid Long value");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("Invalid value 'xyz' for type Long", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_MismatchedMissingType() {
        // Covers: parseMismatchedInput -> both type and val empty -> "Invalid value format"
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Cannot deserialize value of type `SomeType` unexpected token");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("Invalid value format", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_MismatchedTypeOnlyNoValue() {
        // Covers: parseMismatchedInput -> type found but val empty -> "Invalid format for type X"
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        // Has type 'java.lang.Boolean' but no recognizable from String "..." or '...' pattern
        when(ex.getMessage()).thenReturn("Cannot deserialize value of type 'java.lang.Boolean' from token VALUE_NULL");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("Invalid format for type Boolean", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleHttpMessageNotReadableException_ExtractValueMissingSuffix() {
        // Covers: extractValueFromString -> prefix found but suffix not found -> returns ""
        // Message has from String " but missing the closing " -> triggers fallback to single-quote path, also fails -> "Invalid format for type"
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Cannot deserialize value of type 'java.lang.Integer' from String \"unclosed");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertNotNull(response);
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertEquals("Invalid format for type Integer", details.get(0).message());
    }

    @Test
    void testSanitizeWithNewlineAndCarriageReturn() {
        // Covers: sanitize() -> replaces '\n' and '\r' with '_'
        // We inject newline characters in both request URI and exception message to trigger the replace branches
        HttpServletRequest maliciousRequest = mock(HttpServletRequest.class);
        when(maliciousRequest.getRequestURI()).thenReturn("/api/test\ninjected");

        Exception ex = new RuntimeException("Error message\rwith\ncontrol chars");
        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleGenericException(ex, maliciousRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testHandleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = mock(HttpRequestMethodNotSupportedException.class);
        when(ex.getMethod()).thenReturn("GET");
        when(ex.getMessage()).thenReturn("Request method 'GET' is not supported");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleHttpRequestMethodNotSupportedException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("1004", response.getBody().result().responseCode());
        assertEquals("Request is semantically invalid", response.getBody().result().description());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("amount");
        doReturn(Integer.class).when(ex).getRequiredType();
        when(ex.getValue()).thenReturn("abc");
        when(ex.getMessage()).thenReturn("Failed to convert value 'abc' to required type 'Integer'");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleMethodArgumentTypeMismatchException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("1005", response.getBody().result().responseCode());
        
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertNotNull(details);
        assertEquals(1, details.size());
        assertEquals("amount", details.get(0).field());
        assertEquals("Expected type: Integer, provided value: 'abc'", details.get(0).message());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleConstraintViolationException() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);

        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("createUser.email");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("must be a well-formed email address");

        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("amount");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn(null); // triggers default message logic

        when(ex.getConstraintViolations()).thenReturn(Set.of(violation1, violation2));
        when(ex.getMessage()).thenReturn("Validation failed");

        ResponseEntity<ApiResult<Void>> response = exceptionHandler.handleConstraintViolationException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("1001", response.getBody().result().responseCode());
        
        List<ValidationError> details = (List<ValidationError>) response.getBody().error().details();
        assertNotNull(details);
        assertEquals(2, details.size());
        
        boolean foundEmail = false;
        boolean foundAmount = false;
        for (ValidationError err : details) {
            if ("email".equals(err.field())) {
                foundEmail = true;
                assertEquals("must be a well-formed email address", err.message());
            } else if ("amount".equals(err.field())) {
                foundAmount = true;
                assertEquals("Invalid value", err.message());
            }
        }
        assertTrue(foundEmail);
        assertTrue(foundAmount);
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
