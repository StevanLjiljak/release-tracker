package com.releasetracker.controller;

import com.releasetracker.dto.*;
import com.releasetracker.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/releases")
@Tag(name = "Releases", description = "Release management endpoints")
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @GetMapping
    @Operation(summary = "List releases", description = "Retrieve a paginated and filtered list of releases")
    @ApiResponse(responseCode = "200", description = "Releases retrieved successfully")
    public ResponseEntity<PagedResponse<ReleaseResponse>> listReleases(
            @Valid ReleaseFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(releaseService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a release", description = "Retrieve a single release by its ID")
    @ApiResponse(responseCode = "200", description = "Release retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Release not found")
    public ResponseEntity<ReleaseResponse> getRelease(
            @PathVariable @Parameter(description = "Release ID") UUID id) {
        return ResponseEntity.ok(releaseService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a release", description = "Create a new release with initial status 'Created'")
    @ApiResponse(responseCode = "201", description = "Release created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    public ResponseEntity<ReleaseResponse> createRelease(
            @RequestBody @Valid CreateReleaseRequest request) {
        ReleaseResponse response = releaseService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a release", description = "Update an existing release. Only provided fields are updated.")
    @ApiResponse(responseCode = "200", description = "Release updated successfully")
    @ApiResponse(responseCode = "404", description = "Release not found")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    public ResponseEntity<ReleaseResponse> updateRelease(
            @PathVariable @Parameter(description = "Release ID") UUID id,
            @RequestBody @Valid UpdateReleaseRequest request) {
        return ResponseEntity.ok(releaseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a release", description = "Delete a release by its ID")
    @ApiResponse(responseCode = "204", description = "Release deleted successfully")
    @ApiResponse(responseCode = "404", description = "Release not found")
    public ResponseEntity<Void> deleteRelease(
            @PathVariable @Parameter(description = "Release ID") UUID id) {
        releaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
