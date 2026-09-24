package com.vyoog.eisplatform.modules.platform.dto;

import lombok.Getter;
import lombok.Setter;

/** The slice of a Platform embedded in ProductDto — just enough to render a
 * "belongs to" chip on an app card, without pulling in every platform field. */
@Getter
@Setter
public class PlatformSummaryDto {

    private Long id;
    private String name;
}
