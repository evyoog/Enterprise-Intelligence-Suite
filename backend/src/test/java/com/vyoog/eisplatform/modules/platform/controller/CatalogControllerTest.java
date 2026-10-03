package com.vyoog.eisplatform.modules.platform.controller;

import com.vyoog.eisplatform.modules.platform.dto.PlatformCreateRequest;
import com.vyoog.eisplatform.modules.platform.dto.PlatformDto;
import com.vyoog.eisplatform.modules.platform.service.PlatformService;
import com.vyoog.eisplatform.modules.product.dto.ProductCreateRequest;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C66: the public catalog's platforms are derived from real configuration
 * (status, show in catalog, display order, colour, the ACTIVE apps assigned). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PlatformService platformService;
    @Autowired private ProductService productService;

    private PlatformDto platform(String name, String color, String status, Boolean shown, Integer order) {
        return platformService.createPlatform(new PlatformCreateRequest(name, name + " description", null, color, status, shown, order));
    }

    private ProductDto app(String name, Long platformId, ProductStatus status, String category, List<String> tags) {
        return productService.createProduct(new ProductCreateRequest(name, "An app", new BigDecimal("10.00"), null, null, category,
            status, false, List.of(platformId), null, null, null, null, false, null, tags, null, null));
    }

    @Test
    void theCatalogListsVisiblePlatformsWithCountsFromTheirActiveApps() throws Exception {
        String tag = String.valueOf(System.nanoTime());
        PlatformDto visible = platform("Thiran " + tag, "#7c3aed", null, null, 1);
        app("Insights " + tag, visible.getId(), ProductStatus.ACTIVE, "Analytics", List.of("Reports", "Dashboard"));
        app("Planner " + tag, visible.getId(), ProductStatus.ACTIVE, "Planning", List.of("Reports", "Gantt"));
        app("Draft " + tag, visible.getId(), ProductStatus.INACTIVE, "Planning", List.of("Hidden tag"));
        PlatformDto inactive = platform("Inactive " + tag, null, "INACTIVE", true, 0);
        PlatformDto hidden = platform("Hidden " + tag, null, "ACTIVE", false, 0);

        mockMvc.perform(get("/catalog/platforms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + visible.getId() + ")].appCount").value(hasItem(2)))
            .andExpect(jsonPath("$[?(@.id == " + visible.getId() + ")].primaryColor").value(hasItem("#7C3AED")))
            .andExpect(jsonPath("$[*].id").value(not(hasItem(inactive.getId().intValue()))))
            .andExpect(jsonPath("$[*].id").value(not(hasItem(hidden.getId().intValue()))));

        mockMvc.perform(get("/catalog/platforms/" + visible.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories.length()").value(2))
            .andExpect(jsonPath("$.featureTags").value(org.hamcrest.Matchers.contains("Reports", "Dashboard", "Gantt")))
            .andExpect(jsonPath("$.apps.length()").value(2))
            .andExpect(jsonPath("$.apps[0].platforms[0].primaryColor").value("#7C3AED"));

        mockMvc.perform(get("/catalog/platforms/" + hidden.getId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/catalog/platforms/" + inactive.getId())).andExpect(status().isNotFound());
    }

    @Test
    void displayOrderSortsTheCatalog() throws Exception {
        String tag = String.valueOf(System.nanoTime());
        PlatformDto second = platform("Order B " + tag, null, null, null, 9001);
        PlatformDto first = platform("Order A " + tag, null, null, null, 9000);
        String body = mockMvc.perform(get("/catalog/platforms")).andReturn().getResponse().getContentAsString();
        assertThat(body.indexOf("\"id\":" + first.getId() + ",")).isLessThan(body.indexOf("\"id\":" + second.getId() + ","));
    }

    @Test
    void showcaseFieldsAreValidatedAndStored() throws Exception {
        mockMvc.perform(post("/platforms").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Bad colour\",\"primaryColor\":\"purple\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/platforms").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Bad status\",\"status\":\"ARCHIVED\"}"))
            .andExpect(status().isBadRequest());

        PlatformDto p = platform("Stored " + System.nanoTime(), null, null, null, null);
        assertThat(p.getStatus()).isEqualTo("ACTIVE");
        assertThat(p.isShowInCatalog()).isTrue();
        assertThat(p.getPrimaryColor()).isNull();

        ProductDto created = productService.createProduct(new ProductCreateRequest("Tagged " + System.nanoTime(), null,
            new BigDecimal("5.00"), null, null, null, null, false, List.of(p.getId()), null, null, null, null, false,
            "#0891b2", List.of(" Reports ", "Reports", "CRM"), "https://docs.example.com", null));
        assertThat(created.getAccentColor()).isEqualTo("#0891B2");
        assertThat(created.getFeatureTags()).containsExactly("Reports", "CRM");
        assertThat(created.getDocumentationUrl()).isEqualTo("https://docs.example.com");
        assertThat(productService.getProduct(created.getId()).getFeatureTags()).containsExactly("Reports", "CRM");
    }
}
