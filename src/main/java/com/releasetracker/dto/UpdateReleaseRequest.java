package com.releasetracker.dto;

import com.releasetracker.enums.ReleaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request payload for updating an existing release")
public record UpdateReleaseRequest(

        @Size(max = 255, message = "Name must not exceed 255 characters")
        @Schema(description = "Release name", example = "v2.1.0-hotfix")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @Schema(description = "Release description", example = "Updated with hotfix for auth bug")
        String description,

        @Schema(description = "Release status", example = "In Development")
        ReleaseStatus status,

        @Schema(description = "Planned release date", example = "2026-05-15")
        LocalDate releaseDate
) {
}
