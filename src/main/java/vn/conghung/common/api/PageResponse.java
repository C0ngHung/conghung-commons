package vn.conghung.common.api;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Immutable paginated response envelope for all list endpoints.
 * Uses 1-based page numbering for client-facing APIs.
 *
 * <p>Usage in service layer:</p>
 * <pre>{@code
 * Page<User> page = userRepository.findAll(PageRequest.of(normalizedPage - 1, size));
 * return PageResponse.of(normalizedPage, page, userMapper::toDto);
 * }</pre>
 *
 * @param pageNo         Current page number (1-based, as seen by the client).
 * @param pageSize       Number of records per page.
 * @param totalPages     Total number of pages available.
 * @param totalElements  Total number of records in the database.
 * @param items          List of mapped items for the current page (never null).
 * @param <T>            The DTO type returned to clients.
 */
public record PageResponse<T>(
        int pageNo,
        int pageSize,
        long totalPages,
        long totalElements,
        List<T> items
) {

    /**
     * Compact constructor — enforces that items list is never null.
     */
    public PageResponse {
        items = (items != null) ? items : List.of();
    }

    /**
     * Creates a {@code PageResponse} from a Spring Data {@link Page} using a mapping function.
     *
     * <p>This factory method eliminates the repetitive {@code toPageResponse()} helper
     * that each service would otherwise need to write independently.</p>
     *
     * @param pageNo   The 1-based page number used when building the {@link org.springframework.data.domain.Pageable}.
     * @param page     The Spring Data {@link Page} result from the repository.
     * @param mapper   A function to convert each entity {@code E} to the DTO type {@code T}.
     * @param <E>      The entity/domain type returned by the repository.
     * @param <T>      The DTO type to be returned to the client.
     * @return A fully populated, immutable {@code PageResponse<T>}.
     */
    public static <E, T> PageResponse<T> of(int pageNo, Page<E> page, Function<E, T> mapper) {
        List<T> items = page.getContent().stream()
                .map(mapper)
                .toList();
        return new PageResponse<>(pageNo, page.getSize(), page.getTotalPages(), page.getTotalElements(), items);
    }
}
