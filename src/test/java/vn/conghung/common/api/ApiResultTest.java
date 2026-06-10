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
        assertNotNull(result.requestDateTime());
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

    // ── PROPOSAL-001: ApiResult.ok() no-arg ─────────────────────────────────

    @Test
    void ok_noArg_shouldReturnSuccessCode() {
        ApiResult<Void> result = ApiResult.ok();
        assertNotNull(result);
        assertEquals("0000", result.result().responseCode());
    }

    @Test
    void ok_noArg_shouldUseDefaultDescription() {
        ApiResult<Void> result = ApiResult.ok();
        assertEquals("Success", result.result().description());
    }

    @Test
    void ok_noArg_shouldReturnNullDataAndNullError() {
        ApiResult<Void> result = ApiResult.ok();
        assertNull(result.data());
        assertNull(result.error());
    }

    @Test
    void ok_noArg_shouldSetRequestDateTime() {
        ApiResult<Void> result = ApiResult.ok();
        assertNotNull(result.requestDateTime());
    }

    // ── PROPOSAL-003: ApiResult.noData(String) ───────────────────────────────

    @Test
    void noData_withDescription_shouldSetCustomDescription() {
        ApiResult<Void> result = ApiResult.noData("User deleted successfully");
        assertEquals("0000", result.result().responseCode());
        assertEquals("User deleted successfully", result.result().description());
    }

    @Test
    void noData_withDescription_shouldReturnNullDataAndNullError() {
        ApiResult<Void> result = ApiResult.noData("User deleted successfully");
        assertNull(result.data());
        assertNull(result.error());
    }

    /**
     * Critical backward-compatibility test: ApiResult.ok("string") must still resolve
     * to ok(T data) with T=String, NOT to noData(String). This proves no compile-time
     * ambiguity was introduced by naming the new method noData instead of ok(String).
     */
    @Test
    void ok_withStringArgument_shouldResolveToOkTDataNotNoData() {
        ApiResult<String> result = ApiResult.ok("someString");
        // data must be "someString" — if it resolved to noData(), data would be null
        assertEquals("someString", result.data());
        assertNull(result.error());
        assertEquals("0000", result.result().responseCode());
    }
}
