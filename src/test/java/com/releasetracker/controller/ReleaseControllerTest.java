package com.releasetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.releasetracker.dto.*;
import com.releasetracker.enums.ReleaseStatus;
import com.releasetracker.exception.GlobalExceptionHandler;
import com.releasetracker.exception.ReleaseNotFoundException;
import com.releasetracker.service.ReleaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReleaseController.class)
@Import(GlobalExceptionHandler.class)
class ReleaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReleaseService releaseService;

    private static final UUID RELEASE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String BASE_URL = "/api/v1/releases";

    private ReleaseResponse sampleResponse() {
        return new ReleaseResponse(
                RELEASE_ID, "v1.0.0", "Initial release",
                ReleaseStatus.CREATED, LocalDate.of(2026, 5, 1),
                Instant.parse("2026-04-16T10:00:00Z"),
                Instant.parse("2026-04-16T10:00:00Z")
        );
    }

    @Nested
    @DisplayName("GET /api/v1/releases")
    class ListReleases {

        @Test
        @DisplayName("should return paginated releases")
        void shouldReturnPaginatedReleases() throws Exception {
            PagedResponse<ReleaseResponse> response = new PagedResponse<>(
                    List.of(sampleResponse()), 0, 20, 1, 1, true);

            when(releaseService.findAll(any(), any())).thenReturn(response);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("v1.0.0")))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.last", is(true)));
        }

        @Test
        @DisplayName("should accept filter parameters")
        void shouldAcceptFilterParameters() throws Exception {
            PagedResponse<ReleaseResponse> response = new PagedResponse<>(
                    List.of(), 0, 20, 0, 0, true);

            when(releaseService.findAll(any(), any())).thenReturn(response);

            mockMvc.perform(get(BASE_URL)
                            .param("name", "v1")
                            .param("releaseDateFrom", "2026-01-01")
                            .param("releaseDateTo", "2026-12-31"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/releases/{id}")
    class GetRelease {

        @Test
        @DisplayName("should return release when found")
        void shouldReturnRelease() throws Exception {
            when(releaseService.findById(RELEASE_ID)).thenReturn(sampleResponse());

            mockMvc.perform(get(BASE_URL + "/{id}", RELEASE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(RELEASE_ID.toString())))
                    .andExpect(jsonPath("$.name", is("v1.0.0")))
                    .andExpect(jsonPath("$.status", is("Created")));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(releaseService.findById(RELEASE_ID))
                    .thenThrow(new ReleaseNotFoundException(RELEASE_ID));

            mockMvc.perform(get(BASE_URL + "/{id}", RELEASE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Release Not Found")));
        }

        @Test
        @DisplayName("should return 400 for invalid UUID")
        void shouldReturn400ForInvalidUuid() throws Exception {
            mockMvc.perform(get(BASE_URL + "/not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/releases")
    class CreateRelease {

        @Test
        @DisplayName("should create release and return 201")
        void shouldCreateRelease() throws Exception {
            CreateReleaseRequest request = new CreateReleaseRequest(
                    "v2.0.0", "Major release", LocalDate.of(2026, 6, 1));

            when(releaseService.create(any())).thenReturn(sampleResponse());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").exists());
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            CreateReleaseRequest request = new CreateReleaseRequest(
                    "", "Description", null);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", is("Validation failed")));
        }

        @Test
        @DisplayName("should return 400 when name exceeds max length")
        void shouldReturn400WhenNameTooLong() throws Exception {
            String longName = "a".repeat(256);
            CreateReleaseRequest request = new CreateReleaseRequest(
                    longName, "Description", null);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/releases/{id}")
    class UpdateRelease {

        @Test
        @DisplayName("should update release")
        void shouldUpdateRelease() throws Exception {
            UpdateReleaseRequest request = new UpdateReleaseRequest(
                    "v1.0.1", null, ReleaseStatus.IN_DEVELOPMENT, null);

            when(releaseService.update(eq(RELEASE_ID), any())).thenReturn(sampleResponse());

            mockMvc.perform(put(BASE_URL + "/{id}", RELEASE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 404 when updating non-existent release")
        void shouldReturn404WhenNotFound() throws Exception {
            UpdateReleaseRequest request = new UpdateReleaseRequest(
                    "v1.0.1", null, null, null);

            when(releaseService.update(eq(RELEASE_ID), any()))
                    .thenThrow(new ReleaseNotFoundException(RELEASE_ID));

            mockMvc.perform(put(BASE_URL + "/{id}", RELEASE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/releases/{id}")
    class DeleteRelease {

        @Test
        @DisplayName("should delete release and return 204")
        void shouldDeleteRelease() throws Exception {
            doNothing().when(releaseService).delete(RELEASE_ID);

            mockMvc.perform(delete(BASE_URL + "/{id}", RELEASE_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent release")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ReleaseNotFoundException(RELEASE_ID))
                    .when(releaseService).delete(RELEASE_ID);

            mockMvc.perform(delete(BASE_URL + "/{id}", RELEASE_ID))
                    .andExpect(status().isNotFound());
        }
    }
}
