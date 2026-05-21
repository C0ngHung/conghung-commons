package vn.conghung.common.exception;

import org.springframework.http.HttpStatus;

public class IntegrationException extends ApiException {

    private final String externalReference;
    private final String correlationId;

    public IntegrationException(ResponseCode responseCode, String userMessage, String externalReference, String correlationId, Throwable cause) {

        super(responseCode, HttpStatus.BAD_GATEWAY, userMessage, cause);
        this.externalReference = externalReference;
        this.correlationId = correlationId;
    }

    public String externalReference() {
        return externalReference;
    }

    public String correlationId() {
        return correlationId;
    }
}
