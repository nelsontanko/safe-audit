package io.safeaudit.web.dto;

import java.util.List;

/**
 * @author Nelson Tanko
 * @since 1.0.0
 */
public record PageDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public boolean hasNext() {
        return page < totalPages - 1;
    }

    public boolean hasPrevious() {
        return page > 0;
    }
}