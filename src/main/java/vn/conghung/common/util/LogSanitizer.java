package vn.conghung.common.util;

import org.jspecify.annotations.Nullable;

/**
 * Utility for neutralizing CRLF characters in user-supplied strings before they reach the logger.
 *
 * <p>Log-forging (CWE-117 / OWASP CRLF Injection) allows an attacker to inject fake log lines
 * by embedding {@code \r} or {@code \n} inside HTTP request parameters. This class provides
 * a single canonical cleanser that both the commons infrastructure and consumer services
 * can share, eliminating the need for duplicated inline sanitization helpers.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * log.info("GET /orders/{}", LogSanitizer.sanitize(id));
 * }</pre>
 *
 * <p><b>FindSecBugs note:</b> Consumer services must add a {@code spotbugs-exclude.xml} rule
 * for their own controller classes referencing this method. See the project README for details.
 */
public final class LogSanitizer {

    private LogSanitizer() {
        // utility class — not instantiable
    }

    /**
     * Replaces {@code \n} and {@code \r} characters with {@code '_'} to prevent CRLF log injection.
     *
     * @param input user-supplied string (may be null)
     * @return sanitized string, or null if input is null
     */
    public static @Nullable String sanitize(@Nullable String input) {
        if (input == null) {
            return null;
        }
        return input.replace('\n', '_').replace('\r', '_');
    }
}
