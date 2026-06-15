package vn.conghung.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableFactory {

    private static final int MAX_PAGE_SIZE = 100;

    private PageableFactory() {
    }

    public static Pageable of(int page, int size, String sort) {
        int validatedPage = Math.max(1, page) - 1;
        int validatedSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        Sort.Order order = SortParser.parse(sort);
        return PageRequest.of(validatedPage, validatedSize, Sort.by(order));
    }
}
