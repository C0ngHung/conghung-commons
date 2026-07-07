package vn.conghung.common.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Contract tests (TS-018 gap G6): drive real Spring MVC dispatch through {@link GlobalExceptionHandler}
 * and assert the full exception → HTTP status + {@code responseCode} + error shape matrix (AC5),
 * including real Jackson-3 deserialization errors for malformed input (AC3).
 */
class GlobalExceptionHandlerContractTest {

    record UserDto(@NotBlank String name, @NotNull Integer age) {
    }

    @RestController
    static class TestController {
        @PostMapping("/users")
        String createUser(@Valid @RequestBody UserDto dto) {
            return "ok:" + dto.name();
        }

        @GetMapping("/convert")
        String convert(@RequestParam Integer amount) {
            return "amount:" + amount;
        }

        @GetMapping("/tech")
        String tech() {
            throw new TechnicalException(ResponseCode.SYS_INTERNAL_ERROR, "tech down", new RuntimeException("io"));
        }

        @GetMapping("/integration")
        String integration() {
            throw new IntegrationException(ResponseCode.SYS_INTERNAL_ERROR, "partner down", "EXT-1", "CORR-1", new RuntimeException("t/o"));
        }

        @GetMapping("/unknown")
        String unknown() {
            throw new UnknownResultException(ResponseCode.SYS_INTERNAL_ERROR, "TX-1", "EXT-1", "CORR-1", new RuntimeException("t/o"));
        }

        @GetMapping("/business")
        String business() {
            throw new BusinessException(ResponseCode.DATA_CONFLICT, "conflict");
        }

        @GetMapping("/boom")
        String boom() {
            throw new RuntimeException("unexpected");
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        // Use a non-EL message interpolator so the test does not require a Jakarta EL provider.
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        validator.afterPropertiesSet();
        mockMvc = standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void technicalException_maps503() throws Exception {
        mockMvc.perform(get("/tech"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.result.responseCode").value("9999"));
    }

    @Test
    void integrationException_maps502() throws Exception {
        mockMvc.perform(get("/integration"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.result.responseCode").value("9999"));
    }

    @Test
    void unknownResultException_maps202() throws Exception {
        mockMvc.perform(get("/unknown"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.result.responseCode").value("9999"));
    }

    @Test
    void businessException_maps422() throws Exception {
        mockMvc.perform(get("/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result.responseCode").value("4002"));
    }

    @Test
    void validationError_maps400_withFieldDetails() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.responseCode").value("1001"))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    void malformedJson_wrongType_maps400_withStructuredField() throws Exception {
        // age is declared Integer; sending a non-numeric string produces a real Jackson InvalidFormatException
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"neo\",\"age\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.responseCode").value("1002"))
                .andExpect(jsonPath("$.error.details[0].field").value("age"))
                .andExpect(jsonPath("$.error.details[0].message").value("Invalid value 'abc' for type Integer"));
    }

    @Test
    void typeMismatch_maps400() throws Exception {
        mockMvc.perform(get("/convert").param("amount", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.responseCode").value("1005"))
                .andExpect(jsonPath("$.error.details[0].field").value("amount"));
    }

    @Test
    void methodNotSupported_maps405() throws Exception {
        // /users only supports POST; a GET yields HttpRequestMethodNotSupportedException
        mockMvc.perform(get("/users"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.result.responseCode").value("1004"));
    }

    @Test
    void genericException_maps500() throws Exception {
        mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.result.responseCode").value("9999"));
    }
}
