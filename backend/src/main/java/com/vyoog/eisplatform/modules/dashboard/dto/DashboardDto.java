package com.vyoog.eisplatform.modules.dashboard.dto;

import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;

import java.util.List;

/**
 * {@code organization} is null for an individual customer with no org
 * membership — the frontend already branches on this exact null-check today
 * (see MyProductsPage.tsx's own individual/organization split), so this
 * phase keeps that same shape rather than inventing a new discriminator.
 * "Usage summaries" and "business overview" are intentionally NOT separate
 * fields here: business overview IS {@link #organization} (its existing
 * fields already cover this — see this phase's own report on why nothing
 * new was fabricated for it), and usage summaries are the
 * {@code lastLaunchedAt}/{@code launchCount} fields already on each
 * {@link DashboardProductDto}.
 */
public record DashboardDto(
    OrganizationDto organization,
    List<DashboardProductDto> products,
    List<DashboardAlertDto> alerts,
    DashboardPreferenceDto preferences
) {
}
