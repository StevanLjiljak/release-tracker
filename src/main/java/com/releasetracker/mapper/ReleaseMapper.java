package com.releasetracker.mapper;

import com.releasetracker.dto.CreateReleaseRequest;
import com.releasetracker.dto.ReleaseResponse;
import com.releasetracker.entity.Release;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReleaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    Release toEntity(CreateReleaseRequest request);

    ReleaseResponse toResponse(Release entity);
}
