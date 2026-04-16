package com.releasetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request payload for creating a new release")
public record CreateReleaseRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        @Schema(description = "Release name", example = "v2.1.0")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @Schema(description = "Release description", example = "Major feature release with auth improvements")
        String description,

        @Schema(description = "Planned release date", example = "2026-05-01")
        LocalDate releaseDate
) {
}
