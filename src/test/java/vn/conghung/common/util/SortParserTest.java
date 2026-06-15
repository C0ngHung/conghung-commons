package vn.conghung.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SortParserTest {

    @Test
    void testParseAscending() {
        Sort.Order order = SortParser.parse("name:asc");
        assertNotNull(order);
        assertEquals("name", order.getProperty());
        assertTrue(order.isAscending());
    }

    @Test
    void testParseDescending() {
        Sort.Order order = SortParser.parse("createdAt:desc");
        assertNotNull(order);
        assertEquals("createdAt", order.getProperty());
        assertTrue(order.isDescending());
    }

    @Test
    void testParseCaseInsensitiveDirection() {
        Sort.Order order = SortParser.parse("name:ASC");
        assertNotNull(order);
        assertEquals("name", order.getProperty());
        assertTrue(order.isAscending());

        Sort.Order orderDesc = SortParser.parse("name:DESC");
        assertNotNull(orderDesc);
        assertEquals("name", orderDesc.getProperty());
        assertTrue(orderDesc.isDescending());
    }

    @Test
    void testParseInvalidDirectionDefaultsToAscending() {
        Sort.Order order = SortParser.parse("name:xyz");
        assertNotNull(order);
        assertEquals("name", order.getProperty());
        assertTrue(order.isAscending());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "node limiter", ":desc", "  :desc"})
    void testParseDefaultsToIdAsc(String input) {
        Sort.Order order = SortParser.parse(input);
        assertNotNull(order);
        assertEquals("id", order.getProperty());
        assertTrue(order.isAscending());
    }
}
