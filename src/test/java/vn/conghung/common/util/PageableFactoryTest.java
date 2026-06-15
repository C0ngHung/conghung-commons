package vn.conghung.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PageableFactoryTest {

    @Test
    void testOfWithValidData() {
        Pageable pageable = PageableFactory.of(1, 10, "name:asc");
        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        
        Sort sort = pageable.getSort();
        assertNotNull(sort);
        Sort.Order order = sort.getOrderFor("name");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    @Test
    void testOfWithDifferentPageAndSort() {
        Pageable pageable = PageableFactory.of(3, 20, "createdAt:desc");
        assertNotNull(pageable);
        assertEquals(2, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        Sort sort = pageable.getSort();
        assertNotNull(sort);
        Sort.Order order = sort.getOrderFor("createdAt");
        assertNotNull(order);
        assertTrue(order.isDescending());
    }

    @Test
    void testOfWithZeroPage() {
        Pageable pageable = PageableFactory.of(0, 10, "name:asc");
        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void testOfWithNegativePageAndNullSort() {
        Pageable pageable = PageableFactory.of(-1, 15, null);
        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());

        Sort sort = pageable.getSort();
        assertNotNull(sort);
        Sort.Order order = sort.getOrderFor("id");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 1",
        "-5, 1",
        "999, 100"
    })
    void testOfWithSizeClamping(int inputSize, int expectedSize) {
        Pageable pageable = PageableFactory.of(1, inputSize, "name:asc");
        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(expectedSize, pageable.getPageSize());
    }
}
