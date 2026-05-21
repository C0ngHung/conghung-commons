package vn.conghung.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String userMessage) {

        super(ResponseCode.DATA_NOT_FOUND, HttpStatus.NOT_FOUND, userMessage);
    }
}
