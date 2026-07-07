package vn.conghung.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Verifies TS-018 gap G4 / acceptance criterion AC2: because {@link GlobalExceptionHandler} is
 * ordered at {@link Ordered#LOWEST_PRECEDENCE}, a service-specific advice (unordered → higher
 * precedence) is always consulted first for the same exception. Here a service advice handles
 * {@link BusinessException} and must win over the global handler.
 */
class GlobalExceptionHandlerOrderingTest {

    @RestController
    static class ThrowingController {
        @GetMapping("/boom")
        String boom() {
            throw new BusinessException(ResponseCode.DATA_CONFLICT, "from-service");
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RestControllerAdvice
    static class ServiceAdvice {
        @ExceptionHandler(BusinessException.class)
        ResponseEntity<String> handle(BusinessException ex) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                    .header("X-Handled-By", "service-advice")
                    .body(ex.userMessage());
        }
    }

    @Test
    void serviceAdvice_winsOver_globalCatchAll() throws Exception {
        MockMvc mockMvc = standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ServiceAdvice(), new GlobalExceptionHandler())
                .build();

        // If the global handler had won, status would be 422 (UNPROCESSABLE_CONTENT) and no header.
        mockMvc.perform(get("/boom"))
                .andExpect(status().isIAmATeapot())
                .andExpect(header().string("X-Handled-By", "service-advice"));
    }
}
