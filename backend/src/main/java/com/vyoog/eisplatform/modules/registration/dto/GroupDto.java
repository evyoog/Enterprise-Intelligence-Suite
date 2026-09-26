package com.vyoog.eisplatform.modules.registration.dto;

import java.util.List;

public record GroupDto(
    Long id,
    String name,
    List<OrgMemberDto> members
) {
}
