package com.vyoog.eisplatform.modules.platform.mapper;

import com.vyoog.eisplatform.modules.platform.dto.PlatformCreateRequest;
import com.vyoog.eisplatform.modules.platform.dto.PlatformDto;
import com.vyoog.eisplatform.modules.platform.dto.PlatformSummaryDto;
import com.vyoog.eisplatform.modules.platform.model.Platform;
import org.mapstruct.Mapper;

@Mapper
public interface PlatformMapper {

    PlatformDto toDto(Platform platform);

    // Used by ProductMapper to auto-map Product.platforms (Set<Platform>) into
    // ProductDto.platforms (List<PlatformSummaryDto>) — MapStruct picks this up
    // by return/parameter type once both mappers are in the same Spring context.
    PlatformSummaryDto toSummary(Platform platform);

    Platform toEntity(PlatformCreateRequest request);
}
