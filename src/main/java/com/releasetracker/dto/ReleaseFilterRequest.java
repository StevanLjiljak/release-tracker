package com.releasetracker.dto;

import com.releasetracker.enums.ReleaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Filter criteria for listing releases")
public record ReleaseFilterRequest(

        @Schema(description = "Filter by name (partial, case-insensitive)", example = "v2")
        String name,

        @Schema(description = "Filter by status", example = "In Development")
        ReleaseStatus status,

        @Schema(description = "Filter releases from this date", example = "2026-01-01")
        LocalDate releaseDateFrom,

        @Schema(description = "Filter releases up to this date", example = "2026-12-31")
        LocalDate releaseDateTo
) {
}
