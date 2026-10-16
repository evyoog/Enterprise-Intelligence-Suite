package com.vyoog.eisplatform.modules.profile.controller;

import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.CompletionResponse;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileDto;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileUpdateRequest;
import com.vyoog.eisplatform.modules.profile.service.ProfileService;
import com.vyoog.eisplatform.modules.profile.service.ProfileService.PhotoContent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * "My profile" (C107): always the caller's own record, resolved from the JWT (never from an id in the URL or
 * body). Covered by SecurityConfig's existing {@code /me/**} authenticated() rule, so no new rule is needed.
 */
@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileDto get(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return profileService.getMine(jwt, authorities(authentication));
    }

    @PutMapping
    public ProfileDto update(@AuthenticationPrincipal Jwt jwt, Authentication authentication,
                             @RequestBody ProfileUpdateRequest request) {
        return profileService.updateMine(jwt, authorities(authentication), request);
    }

    /** Cheap read behind the global profile bar. */
    @GetMapping("/completion")
    public CompletionResponse completion(@AuthenticationPrincipal Jwt jwt) {
        return profileService.completionMine(jwt);
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> photo(@AuthenticationPrincipal Jwt jwt,
                                        @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        PhotoContent photo = profileService.getPhoto(jwt);
        // "private, no-cache" revalidates on every use, i.e. max-age=0 + must-revalidate semantics for a private cache.
        CacheControl cacheControl = CacheControl.maxAge(0, java.util.concurrent.TimeUnit.SECONDS).cachePrivate().mustRevalidate();
        if (photo.etag().equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).cacheControl(cacheControl).eTag(photo.etag()).build();
        }
        return ResponseEntity.ok()
            .cacheControl(cacheControl)
            .eTag(photo.etag())
            .header("X-Content-Type-Options", "nosniff")
            .contentType(MediaType.parseMediaType(photo.contentType()))
            .body(photo.data());
    }

    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDto uploadPhoto(@AuthenticationPrincipal Jwt jwt, Authentication authentication,
                                  @RequestParam("file") MultipartFile file) {
        return profileService.putPhoto(jwt, authorities(authentication), file);
    }

    @DeleteMapping("/photo")
    public ProfileDto removePhoto(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return profileService.removePhoto(jwt, authorities(authentication));
    }

    static List<String> authorities(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }
}
