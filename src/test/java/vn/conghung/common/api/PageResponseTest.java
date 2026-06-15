package vn.conghung.common.api;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResponseTest {

    @Test
    void testConstructorWithValidData() {
        List<String> items = List.of("item1", "item2");
        PageResponse<String> response = new PageResponse<>(1, 10, 2, 20L, items);

        assertEquals(1, response.pageNo());
        assertEquals(10, response.pageSize());
        assertEquals(2, response.totalPages());
        assertEquals(20L, response.totalElements());
        assertEquals(items, response.items());
    }

    @Test
    void testNullItemsFallbackToEmptyList() {
        PageResponse<String> response = new PageResponse<>(1, 10, 0, 0L, null);

        assertNotNull(response.items());
        assertTrue(response.items().isEmpty());
    }

    @Test
    void testFactoryMethodWithSpringPage() {
        List<String> entities = List.of("one", "two", "three");
        PageRequest pageable = PageRequest.of(0, 10);
        Page<String> springPage = new PageImpl<>(entities, pageable, 25L);

        // Factory method should map String elements to uppercase
        PageResponse<String> response = PageResponse.of(1, springPage, String::toUpperCase);

        assertEquals(1, response.pageNo());
        assertEquals(10, response.pageSize());
        assertEquals(3, response.totalPages()); // 25 total elements / 10 page size = 3 pages
        assertEquals(25L, response.totalElements());
        
        List<String> expectedItems = List.of("ONE", "TWO", "THREE");
        assertEquals(expectedItems, response.items());
    }

    @Test
    void testFactoryMethodWithEmptySpringPage() {
        List<String> emptyList = List.of();
        PageRequest pageable = PageRequest.of(0, 10);
        Page<String> springPage = new PageImpl<>(emptyList, pageable, 0L);

        PageResponse<Integer> response = PageResponse.of(1, springPage, String::length);

        assertEquals(1, response.pageNo());
        assertEquals(10, response.pageSize());
        assertEquals(0, response.totalPages());
        assertEquals(0L, response.totalElements());
        assertNotNull(response.items());
        assertTrue(response.items().isEmpty());
    }
}
