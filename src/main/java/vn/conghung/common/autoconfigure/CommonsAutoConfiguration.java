package vn.conghung.common.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import vn.conghung.common.exception.GlobalExceptionHandler;

/**
 * Auto-configuration for conghung-commons.
 *
 * <p>Registers the shared infrastructure beans so a consumer service gets them
 * simply by depending on this library — <b>independent of the consumer's package root</b>.
 * Previously {@link GlobalExceptionHandler} loaded only because every consumer happened to
 * root its component scan at {@code vn.conghung}; a service in another package silently lost
 * the handler and fell back to HTTP 500 (see troubleshooting TS-001 / TS-018 gap G1).
 *
 * <p>Discovered by Spring Boot via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 *
 * @since 0.3.1
 */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class CommonsAutoConfiguration {
}
