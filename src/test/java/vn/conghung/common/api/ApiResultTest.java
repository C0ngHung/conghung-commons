package vn.conghung.common.api;

import org.junit.jupiter.api.Test;
import vn.conghung.common.exception.ResponseCode;

import static org.junit.jupiter.api.Assertions.*;

class ApiResultTest {

    @Test
    void testOkWithData() {
        ApiResult<String> result = ApiResult.ok("test-data");
        assertNotNull(result);
        assertEquals("0000", result.result().responseCode());
        assertEquals("Success", result.result().description());
        assertEquals("test-data", result.data());
        assertNull(result.error());
        assertNotNull(result.timestamp());
    }

    @Test
    void testOkWithDescriptionAndData() {
        ApiResult<String> result = ApiResult.ok("Custom Success Description", "test-data");
        assertNotNull(result);
        assertEquals("0000", result.result().responseCode());
        assertEquals("Custom Success Description", result.result().description());
        assertEquals("test-data", result.data());
        assertNull(result.error());
    }

    @Test
    void testFailWithResponseCode() {
        ApiResult<Void> result = ApiResult.fail(ResponseCode.DATA_NOT_FOUND);
        assertNotNull(result);
        assertEquals("4001", result.result().responseCode());
        assertEquals("Resource not found", result.result().description());
        assertNull(result.data());
        assertNull(result.error());
    }

    @Test
    void testFailWithResponseCodeAndDescription() {
        ApiResult<Void> result = ApiResult.fail(ResponseCode.PERM_FORBIDDEN, "Custom Forbidden Message");
        assertNotNull(result);
        assertEquals("2101", result.result().responseCode());
        assertEquals("Custom Forbidden Message", result.result().description());
        assertNull(result.data());
        assertNull(result.error());
    }

    @Test
    void testFailWithResponseCodeDescriptionAndDetails() {
        Object details = "Additional validation details";
        ApiResult<Void> result = ApiResult.fail(ResponseCode.REQ_VALIDATION_ERROR, "Invalid Data", details);
        assertNotNull(result);
        assertEquals("1001", result.result().responseCode());
        assertEquals("Invalid Data", result.result().description());
        assertNull(result.data());
        assertNotNull(result.error());
        assertEquals(details, result.error().details());
    }

    @Test
    void testResultInfoOf() {
        ResultInfo info = ResultInfo.of(ResponseCode.COMMON_SUCCESS);
        assertEquals("0000", info.responseCode());
        assertEquals("Success", info.description());

        ResultInfo customInfo = ResultInfo.of(ResponseCode.SYS_INTERNAL_ERROR, "Custom Error");
        assertEquals("9999", customInfo.responseCode());
        assertEquals("Custom Error", customInfo.description());
    }

    @Test
    void testErrorDetailOf() {
        ErrorDetail error = ErrorDetail.of("detail-object");
        assertEquals("detail-object", error.details());
    }
}
