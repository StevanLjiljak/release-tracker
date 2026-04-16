package com.releasetracker.service;

import com.releasetracker.dto.*;
import com.releasetracker.entity.Release;
import com.releasetracker.exception.ReleaseNotFoundException;
import com.releasetracker.mapper.ReleaseMapper;
import com.releasetracker.repository.ReleaseRepository;
import com.releasetracker.specification.ReleaseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReleaseService {

    private static final Logger log = LoggerFactory.getLogger(ReleaseService.class);

    private final ReleaseRepository repository;
    private final ReleaseMapper mapper;

    public ReleaseService(ReleaseRepository repository, ReleaseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public PagedResponse<ReleaseResponse> findAll(ReleaseFilterRequest filter, Pageable pageable) {
        log.debug("Finding releases with filter: {}, pageable: {}", filter, pageable);

        Page<Release> page = repository.findAll(
                ReleaseSpecification.withFilters(filter), pageable);

        return new PagedResponse<>(
                page.getContent().stream().map(mapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public ReleaseResponse findById(UUID id) {
        log.debug("Finding release by id: {}", id);

        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ReleaseNotFoundException(id));
    }

    @Transactional
    public ReleaseResponse create(CreateReleaseRequest request) {
        log.info("Creating release: {}", request.name());

        Release release = mapper.toEntity(request);
        Release saved = repository.save(release);

        log.info("Created release with id: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional
    public ReleaseResponse update(UUID id, UpdateReleaseRequest request) {
        log.info("Updating release: {}", id);

        Release release = repository.findById(id)
                .orElseThrow(() -> new ReleaseNotFoundException(id));

        if (request.name() != null) {
            release.setName(request.name());
        }
        if (request.description() != null) {
            release.setDescription(request.description());
        }
        if (request.status() != null) {
            release.setStatus(request.status());
        }
        if (request.releaseDate() != null) {
            release.setReleaseDate(request.releaseDate());
        }

        Release saved = repository.save(release);
        log.info("Updated release: {}", id);
        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting release: {}", id);

        if (!repository.existsById(id)) {
            throw new ReleaseNotFoundException(id);
        }

        repository.deleteById(id);
        log.info("Deleted release: {}", id);
    }
}
