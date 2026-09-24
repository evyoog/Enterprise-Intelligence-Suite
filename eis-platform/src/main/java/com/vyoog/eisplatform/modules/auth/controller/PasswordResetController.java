package com.vyoog.eisplatform.modules.auth.controller;

import com.vyoog.eisplatform.modules.auth.dto.ForgotPasswordRequest;
import com.vyoog.eisplatform.modules.auth.dto.ResetPasswordRequest;
import com.vyoog.eisplatform.modules.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 8: entirely public — permitAll under the existing {@code /auth/**}
 * rule in SecurityConfig, same tier as login itself (nobody has a token yet
 * at this point). Both endpoints return no informative body: 202 always for
 * the forgot-password request (see PasswordResetService's anti-enumeration
 * handling), 204 on a successful reset.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword(), request.confirmPassword());
    }
}
