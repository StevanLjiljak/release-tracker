package com.releasetracker.entity;

import com.releasetracker.enums.ReleaseStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "releases", indexes = {
        @Index(name = "idx_releases_status", columnList = "status"),
        @Index(name = "idx_releases_release_date", columnList = "release_date"),
        @Index(name = "idx_releases_name", columnList = "name")
})
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReleaseStatus status;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    public Release() {
    }

    public Release(UUID id, String name, String description, ReleaseStatus status,
                   LocalDate releaseDate, Instant createdAt, Instant lastUpdatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.releaseDate = releaseDate;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReleaseStatus getStatus() { return status; }
    public void setStatus(ReleaseStatus status) { this.status = status; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(Instant lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Release release = (Release) o;
        return Objects.equals(id, release.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Release{id=" + id + ", name='" + name + "', status=" + status + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String description;
        private ReleaseStatus status;
        private LocalDate releaseDate;
        private Instant createdAt;
        private Instant lastUpdatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(ReleaseStatus status) { this.status = status; return this; }
        public Builder releaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastUpdatedAt(Instant lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; return this; }

        public Release build() {
            return new Release(id, name, description, status, releaseDate, createdAt, lastUpdatedAt);
        }
    }
}
