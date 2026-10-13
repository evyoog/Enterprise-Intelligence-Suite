package com.vyoog.eisplatform.modules.productcontent.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Product content settings from {@code eis.product-content.*} in
 * application.yml, each overridable by an environment variable (C81).
 * Files use the knowledge storage bucket (eis.knowledge.storage.*); access
 * keys are never settings (BR-SEC-001).
 */
@Component
@Getter
public class ProductContentSettings {

    /** PNG, JPG, WebP (REQ-CAT-004.5; PC-6 default). */
    @Value("${eis.product-content.image-max-size:5242880}")
    private long imageMaxSize;

    /** Datasheet and case-study PDF (REQ-CAT-004.2; PC-6 default). */
    @Value("${eis.product-content.document-max-size:20971520}")
    private long documentMaxSize;

    /** Hosted video (PC-3, C89): one presigned upload, so keep it well under the 5 GB S3 limit. */
    @Value("${eis.product-content.video-max-size:524288000}")
    private long videoMaxSize;

    /** Playback link of a hosted video; long enough to watch and seek through a full video. */
    @Value("${eis.product-content.playback-url-expiry:PT2H}")
    private Duration playbackUrlExpiry;

    @Value("${eis.product-content.upload-url-expiry:PT15M}")
    private Duration uploadUrlExpiry;

    /** Download links for datasheets and case-study PDFs. */
    @Value("${eis.product-content.download-url-expiry:PT5M}")
    private Duration downloadUrlExpiry;

    /** Image links shown on the product page. */
    @Value("${eis.product-content.view-url-expiry:PT1H}")
    private Duration viewUrlExpiry;
}
