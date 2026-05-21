package vn.conghung.common.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    private final ResponseCode responseCode;
    private final HttpStatus httpStatus;
    private final String userMessage;

    protected ApiException(ResponseCode responseCode, HttpStatus httpStatus) {

        super(responseCode.defaultMessage());
        this.responseCode = responseCode;
        this.httpStatus = httpStatus;
        this.userMessage = responseCode.defaultMessage();
    }

    protected ApiException(ResponseCode responseCode, HttpStatus httpStatus, String userMessage) {

        super(userMessage);
        this.responseCode = responseCode;
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }

    protected ApiException(ResponseCode responseCode, HttpStatus httpStatus, String userMessage, Throwable cause) {

        super(userMessage, cause);
        this.responseCode = responseCode;
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }

    public ResponseCode responseCode() {
        return responseCode;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String userMessage() {
        return userMessage;
    }
}
