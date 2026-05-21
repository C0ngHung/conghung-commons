package vn.conghung.common.exception;

import org.springframework.http.HttpStatus;

public class UnknownResultException extends ApiException {

    private final String transactionReference;
    private final String externalReference;
    private final String correlationId;

    public UnknownResultException(ResponseCode responseCode, String transactionReference, String externalReference, String correlationId, Throwable cause) {

        super(responseCode, HttpStatus.ACCEPTED, responseCode.defaultMessage(), cause);
        this.transactionReference = transactionReference;
        this.externalReference = externalReference;
        this.correlationId = correlationId;
    }

    public String transactionReference() {
        return transactionReference;
    }

    public String externalReference() {
        return externalReference;
    }

    public String correlationId() {
        return correlationId;
    }
}
