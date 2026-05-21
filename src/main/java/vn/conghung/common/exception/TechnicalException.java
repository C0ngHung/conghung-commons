package vn.conghung.common.exception;

import org.springframework.http.HttpStatus;

public class TechnicalException extends ApiException {

    public TechnicalException(ResponseCode responseCode, String userMessage, Throwable cause) {

        super(responseCode, HttpStatus.SERVICE_UNAVAILABLE, userMessage, cause);
    }
}
