package com.releasetracker.service;

import com.releasetracker.dto.*;
import com.releasetracker.entity.Release;
import com.releasetracker.enums.ReleaseStatus;
import com.releasetracker.exception.ReleaseNotFoundException;
import com.releasetracker.mapper.ReleaseMapper;
import com.releasetracker.repository.ReleaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseServiceTest {

    @Mock
    private ReleaseRepository repository;

    @Mock
    private ReleaseMapper mapper;

    @InjectMocks
    private ReleaseService service;

    private UUID releaseId;
    private Release release;
    private ReleaseResponse releaseResponse;

    @BeforeEach
    void setUp() {
        releaseId = UUID.randomUUID();
        release = Release.builder()
                .id(releaseId)
                .name("v1.0.0")
                .description("Initial release")
                .status(ReleaseStatus.CREATED)
                .releaseDate(LocalDate.of(2026, 5, 1))
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();

        releaseResponse = new ReleaseResponse(
                releaseId, "v1.0.0", "Initial release",
                ReleaseStatus.CREATED, LocalDate.of(2026, 5, 1),
                release.getCreatedAt(), release.getLastUpdatedAt()
        );
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return paginated releases with filters applied")
        void shouldReturnPaginatedReleases() {
            ReleaseFilterRequest filter = new ReleaseFilterRequest("v1", null, null, null);
            Pageable pageable = PageRequest.of(0, 20);
            Page<Release> page = new PageImpl<>(List.of(release), pageable, 1);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(mapper.toResponse(release)).thenReturn(releaseResponse);

            PagedResponse<ReleaseResponse> result = service.findAll(filter, pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo("v1.0.0");
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isZero();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("should return empty page when no releases match")
        void shouldReturnEmptyPage() {
            ReleaseFilterRequest filter = new ReleaseFilterRequest(null, null, null, null);
            Pageable pageable = PageRequest.of(0, 20);
            Page<Release> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            PagedResponse<ReleaseResponse> result = service.findAll(filter, pageable);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return release when found")
        void shouldReturnRelease() {
            when(repository.findById(releaseId)).thenReturn(Optional.of(release));
            when(mapper.toResponse(release)).thenReturn(releaseResponse);

            ReleaseResponse result = service.findById(releaseId);

            assertThat(result.id()).isEqualTo(releaseId);
            assertThat(result.name()).isEqualTo("v1.0.0");
        }

        @Test
        @DisplayName("should throw ReleaseNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(repository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(unknownId))
                    .isInstanceOf(ReleaseNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create release with CREATED status")
        void shouldCreateRelease() {
            CreateReleaseRequest request = new CreateReleaseRequest(
                    "v2.0.0", "Major release", LocalDate.of(2026, 6, 1));

            when(mapper.toEntity(request)).thenReturn(release);
            when(repository.save(release)).thenReturn(release);
            when(mapper.toResponse(release)).thenReturn(releaseResponse);

            ReleaseResponse result = service.create(request);

            assertThat(result).isNotNull();
            verify(mapper).toEntity(request);
            verify(repository).save(release);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update only provided fields")
        void shouldUpdateProvidedFields() {
            UpdateReleaseRequest request = new UpdateReleaseRequest(
                    "v1.0.1", null, ReleaseStatus.IN_DEVELOPMENT, null);

            when(repository.findById(releaseId)).thenReturn(Optional.of(release));
            when(repository.save(release)).thenReturn(release);
            when(mapper.toResponse(release)).thenReturn(releaseResponse);

            service.update(releaseId, request);

            assertThat(release.getName()).isEqualTo("v1.0.1");
            assertThat(release.getStatus()).isEqualTo(ReleaseStatus.IN_DEVELOPMENT);
            assertThat(release.getDescription()).isEqualTo("Initial release"); // unchanged
            verify(repository).save(release);
        }

        @Test
        @DisplayName("should throw ReleaseNotFoundException when updating non-existent release")
        void shouldThrowWhenUpdatingNotFound() {
            UUID unknownId = UUID.randomUUID();
            UpdateReleaseRequest request = new UpdateReleaseRequest("name", null, null, null);

            when(repository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(unknownId, request))
                    .isInstanceOf(ReleaseNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete existing release")
        void shouldDeleteRelease() {
            when(repository.existsById(releaseId)).thenReturn(true);

            service.delete(releaseId);

            verify(repository).deleteById(releaseId);
        }

        @Test
        @DisplayName("should throw ReleaseNotFoundException when deleting non-existent release")
        void shouldThrowWhenDeletingNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(repository.existsById(unknownId)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(unknownId))
                    .isInstanceOf(ReleaseNotFoundException.class);

            verify(repository, never()).deleteById(any());
        }
    }
}
