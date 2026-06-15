package vn.conghung.common.api;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        int pageNo,
        int pageSize,
        long totalPages,
        long totalElements,
        List<T> items
) {

    public PageResponse {
        items = (items != null) ? List.copyOf(items) : List.of();
    }

    public static <E, T> PageResponse<T> of(int pageNo, Page<E> page, Function<E, T> mapper) {
        List<T> items = page.getContent().stream()
                .map(mapper)
                .toList();
        return new PageResponse<>(pageNo, page.getSize(), page.getTotalPages(), page.getTotalElements(), items);
    }
}
