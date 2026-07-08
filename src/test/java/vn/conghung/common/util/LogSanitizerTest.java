package vn.conghung.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogSanitizerTest {

    @Test
    void sanitize_null_returnsNull() {
        assertThat(LogSanitizer.sanitize(null)).isNull();
    }

    @Test
    void sanitize_plainString_returnsUnchanged() {
        assertThat(LogSanitizer.sanitize("abc-123")).isEqualTo("abc-123");
    }

    @Test
    void sanitize_stringWithLineFeed_replacesWithUnderscore() {
        assertThat(LogSanitizer.sanitize("abc\n123")).isEqualTo("abc_123");
    }

    @Test
    void sanitize_stringWithCarriageReturn_replacesWithUnderscore() {
        assertThat(LogSanitizer.sanitize("abc\r123")).isEqualTo("abc_123");
    }

    @Test
    void sanitize_stringWithCRLF_replacesBoth() {
        assertThat(LogSanitizer.sanitize("abc\r\n123")).isEqualTo("abc__123");
    }

    @Test
    void sanitize_injectedLogLine_neutralized() {
        // Simulates attacker payload: "id\r\n[SECURITY] Admin logged in"
        String attackerInput = "abc123\r\n[SECURITY] Admin logged in as root";
        String result = LogSanitizer.sanitize(attackerInput);

        assertThat(result).doesNotContain("\r", "\n");
        assertThat(result).isEqualTo("abc123__[SECURITY] Admin logged in as root");
    }

    @Test
    void sanitize_emptyString_returnsEmpty() {
        assertThat(LogSanitizer.sanitize("")).isEqualTo("");
    }
}
