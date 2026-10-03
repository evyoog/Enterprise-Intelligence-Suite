package com.vyoog.eisplatform.modules.platform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformDto {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String primaryColor;
    private String status;
    private boolean showInCatalog;
    private int displayOrder;
}
