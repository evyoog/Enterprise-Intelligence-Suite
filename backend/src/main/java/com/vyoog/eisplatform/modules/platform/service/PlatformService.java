package com.vyoog.eisplatform.modules.platform.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.platform.dto.PlatformCreateRequest;
import com.vyoog.eisplatform.modules.platform.dto.PlatformDto;
import com.vyoog.eisplatform.modules.platform.mapper.PlatformMapper;
import com.vyoog.eisplatform.modules.platform.model.Platform;
import com.vyoog.eisplatform.modules.platform.repository.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformService {

    private final PlatformRepository platformRepository;
    private final PlatformMapper platformMapper;

    /** Admin-only listing — every high-level platform (e.g. Thittam). */
    public List<PlatformDto> listPlatforms() {
        return platformRepository.findAll().stream()
            .map(platformMapper::toDto)
            .toList();
    }

    public PlatformDto getPlatform(Long id) {
        return platformRepository.findById(id)
            .map(platformMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Platform not found: " + id));
    }

    @Transactional
    public PlatformDto createPlatform(PlatformCreateRequest request) {
        Platform platform = platformMapper.toEntity(request);
        Platform saved = platformRepository.save(platform);
        return platformMapper.toDto(saved);
    }

    @Transactional
    public PlatformDto updatePlatform(Long id, PlatformCreateRequest request) {
        Platform platform = platformRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Platform not found: " + id));

        platform.setName(request.name());
        platform.setDescription(request.description());
        platform.setImageUrl(request.imageUrl());

        Platform saved = platformRepository.save(platform);
        return platformMapper.toDto(saved);
    }

    /** Nothing else references a platform except the product_platforms join
     * table, which cascades at the DB level (ON DELETE CASCADE) — unlike
     * Product, there's no dependent business history that could be orphaned. */
    @Transactional
    public void deletePlatform(Long id) {
        if (!platformRepository.existsById(id)) {
            throw new ResourceNotFoundException("Platform not found: " + id);
        }
        platformRepository.deleteById(id);
    }
}
