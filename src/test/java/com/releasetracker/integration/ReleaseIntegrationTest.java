package com.releasetracker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.releasetracker.dto.CreateReleaseRequest;
import com.releasetracker.dto.UpdateReleaseRequest;
import com.releasetracker.entity.Release;
import com.releasetracker.enums.ReleaseStatus;
import com.releasetracker.repository.ReleaseRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReleaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("release_tracker_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReleaseRepository releaseRepository;

    private static final String BASE_URL = "/api/v1/releases";

    @BeforeEach
    void cleanUp() {
        releaseRepository.deleteAll();
    }

    @Test
    @DisplayName("Full CRUD lifecycle: create, read, update, list, delete")
    void fullCrudLifecycle() throws Exception {
        // 1. CREATE
        CreateReleaseRequest createRequest = new CreateReleaseRequest(
                "v3.0.0", "Major feature release", LocalDate.of(2026, 7, 1));

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("v3.0.0")))
                .andExpect(jsonPath("$.status", is("Created")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.lastUpdatedAt").exists())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        String releaseId = objectMapper.readTree(responseBody).get("id").asText();

        // 2. GET by ID
        mockMvc.perform(get(BASE_URL + "/{id}", releaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(releaseId)))
                .andExpect(jsonPath("$.name", is("v3.0.0")))
                .andExpect(jsonPath("$.description", is("Major feature release")));

        // 3. UPDATE
        UpdateReleaseRequest updateRequest = new UpdateReleaseRequest(
                "v3.0.0-rc1", "Release candidate", ReleaseStatus.IN_DEVELOPMENT, LocalDate.of(2026, 7, 15));

        mockMvc.perform(put(BASE_URL + "/{id}", releaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("v3.0.0-rc1")))
                .andExpect(jsonPath("$.status", is("In Development")))
                .andExpect(jsonPath("$.description", is("Release candidate")));

        // 4. LIST with filter
        mockMvc.perform(get(BASE_URL).param("name", "v3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("v3.0.0-rc1")));

        // 5. DELETE
        mockMvc.perform(delete(BASE_URL + "/{id}", releaseId))
                .andExpect(status().isNoContent());

        // 6. Verify deleted
        mockMvc.perform(get(BASE_URL + "/{id}", releaseId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should filter releases by status")
    void shouldFilterByStatus() throws Exception {
        createRelease("Alpha", ReleaseStatus.CREATED);
        createRelease("Beta", ReleaseStatus.IN_DEVELOPMENT);
        createRelease("RC", ReleaseStatus.ON_DEV);

        mockMvc.perform(get(BASE_URL).param("status", "In Development"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Beta")));
    }

    @Test
    @DisplayName("Should filter releases by date range")
    void shouldFilterByDateRange() throws Exception {
        Release r1 = Release.builder()
                .name("Q1 Release").status(ReleaseStatus.CREATED)
                .releaseDate(LocalDate.of(2026, 3, 1)).build();
        Release r2 = Release.builder()
                .name("Q2 Release").status(ReleaseStatus.CREATED)
                .releaseDate(LocalDate.of(2026, 6, 1)).build();
        Release r3 = Release.builder()
                .name("Q4 Release").status(ReleaseStatus.CREATED)
                .releaseDate(LocalDate.of(2026, 12, 1)).build();
        releaseRepository.saveAll(java.util.List.of(r1, r2, r3));

        mockMvc.perform(get(BASE_URL)
                        .param("releaseDateFrom", "2026-04-01")
                        .param("releaseDateTo", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Q2 Release")));
    }

    @Test
    @DisplayName("Should support pagination")
    void shouldSupportPagination() throws Exception {
        for (int i = 1; i <= 25; i++) {
            createRelease("Release-" + String.format("%02d", i), ReleaseStatus.CREATED);
        }

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", is(25)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.last", is(false)));

        mockMvc.perform(get(BASE_URL)
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    @DisplayName("Should return 400 for validation errors")
    void shouldReturn400ForValidationErrors() throws Exception {
        CreateReleaseRequest invalid = new CreateReleaseRequest("", null, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Error")));
    }

    @Test
    @DisplayName("Should verify database state after operations")
    void shouldVerifyDatabaseState() throws Exception {
        CreateReleaseRequest request = new CreateReleaseRequest(
                "DB Test Release", "Testing DB state", LocalDate.of(2026, 8, 1));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(releaseRepository.count()).isEqualTo(1);

        Release saved = releaseRepository.findAll().get(0);
        assertThat(saved.getName()).isEqualTo("DB Test Release");
        assertThat(saved.getStatus()).isEqualTo(ReleaseStatus.CREATED);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getLastUpdatedAt()).isNotNull();
    }

    private void createRelease(String name, ReleaseStatus status) {
        Release release = Release.builder()
                .name(name)
                .status(status)
                .releaseDate(LocalDate.of(2026, 6, 1))
                .build();
        releaseRepository.save(release);
    }
}
