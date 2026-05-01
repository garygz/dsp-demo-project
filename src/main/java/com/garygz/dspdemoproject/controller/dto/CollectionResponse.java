package com.garygz.dspdemoproject.controller.dto;

import java.util.List;

/**
 * Standard envelope for all collection endpoints.
 * { "data": [...], "total": N, "page": 0, "pageSize": N }
 *
 * page and pageSize are placeholders for future server-side pagination.
 * Currently all items are returned in a single page.
 */
public record CollectionResponse<T>(List<T> data, int total, int page, int pageSize) {

    public static <T> CollectionResponse<T> of(List<T> items) {
        return new CollectionResponse<>(items, items.size(), 0, items.size());
    }
}
