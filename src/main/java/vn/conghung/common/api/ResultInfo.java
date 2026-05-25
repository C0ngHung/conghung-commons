package vn.conghung.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import vn.conghung.common.exception.ResponseCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResultInfo(String responseCode, String description) {

    public static ResultInfo of(ResponseCode code) {

        return new ResultInfo(code.code(), code.defaultMessage());
    }

    public static ResultInfo of(ResponseCode code, String description) {

        return new ResultInfo(code.code(), description);
    }
}
