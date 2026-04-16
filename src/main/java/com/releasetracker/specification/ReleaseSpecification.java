package com.releasetracker.specification;

import com.releasetracker.dto.ReleaseFilterRequest;
import com.releasetracker.entity.Release;
import com.releasetracker.enums.ReleaseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ReleaseSpecification {

    private ReleaseSpecification() {
    }

    public static Specification<Release> withFilters(ReleaseFilterRequest filter) {
        return Specification.where(hasNameLike(filter.name()))
                .and(hasStatus(filter.status()))
                .and(hasReleaseDateFrom(filter.releaseDateFrom()))
                .and(hasReleaseDateTo(filter.releaseDateTo()));
    }

    private static Specification<Release> hasNameLike(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<Release> hasStatus(ReleaseStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Release> hasReleaseDateFrom(LocalDate from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("releaseDate"), from);
    }

    private static Specification<Release> hasReleaseDateTo(LocalDate to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("releaseDate"), to);
    }
}
