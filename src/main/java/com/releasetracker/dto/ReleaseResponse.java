package com.releasetracker.dto;

import com.releasetracker.enums.ReleaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Release response payload")
public record ReleaseResponse(

        @Schema(description = "Release unique identifier")
        UUID id,

        @Schema(description = "Release name", example = "v2.1.0")
        String name,

        @Schema(description = "Release description")
        String description,

        @Schema(description = "Current release status", example = "In Development")
        ReleaseStatus status,

        @Schema(description = "Planned release date")
        LocalDate releaseDate,

        @Schema(description = "Timestamp when the release was created")
        Instant createdAt,

        @Schema(description = "Timestamp when the release was last updated")
        Instant lastUpdatedAt
) {
}
