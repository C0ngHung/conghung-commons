package vn.conghung.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(Object details) {

    public static ErrorDetail of(Object details) {
        return new ErrorDetail(details);
    }
}
