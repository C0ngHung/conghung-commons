package vn.conghung.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {

    public BusinessException(ResponseCode responseCode) {

        super(responseCode, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    public BusinessException(ResponseCode responseCode, String userMessage) {

        super(responseCode, HttpStatus.UNPROCESSABLE_CONTENT, userMessage);
    }

    public BusinessException(ResponseCode responseCode, HttpStatus httpStatus, String userMessage) {

        super(responseCode, httpStatus, userMessage);
    }
}
