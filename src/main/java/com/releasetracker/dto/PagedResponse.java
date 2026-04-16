package com.releasetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response wrapper")
public record PagedResponse<T>(

        @Schema(description = "Page content")
        List<T> content,

        @Schema(description = "Current page number (0-indexed)")
        int page,

        @Schema(description = "Page size")
        int size,

        @Schema(description = "Total number of elements")
        long totalElements,

        @Schema(description = "Total number of pages")
        int totalPages,

        @Schema(description = "Whether this is the last page")
        boolean last
) {
}
