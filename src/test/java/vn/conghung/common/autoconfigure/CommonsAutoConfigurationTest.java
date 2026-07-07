package vn.conghung.common.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import vn.conghung.common.exception.GlobalExceptionHandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies TS-018 gap G1 / acceptance criterion AC1: the {@link GlobalExceptionHandler}
 * loads purely from the auto-configuration, with NO component scan involved. The
 * {@link ApplicationContextRunner} never scans any package — it only loads what is handed to it
 * via {@link AutoConfigurations}. So if the handler bean is present, it proves the library is
 * portable regardless of the consumer's package root.
 */
class CommonsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonsAutoConfiguration.class));

    @Test
    void globalExceptionHandler_isRegistered_viaAutoConfiguration_withoutComponentScan() {
        contextRunner.run(context ->
                assertDoesNotThrow(() -> assertNotNull(context.getBean(GlobalExceptionHandler.class))));
    }

    @Test
    void autoConfiguration_startsSuccessfully() {
        contextRunner.run(context ->
                assertNotNull(context.getBean(CommonsAutoConfiguration.class)));
    }
}
