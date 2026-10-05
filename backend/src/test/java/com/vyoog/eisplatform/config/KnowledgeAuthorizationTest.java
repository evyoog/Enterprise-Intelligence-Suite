package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.authorization.model.Permission;
import com.vyoog.eisplatform.modules.authorization.model.Role;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import com.vyoog.eisplatform.modules.authorization.repository.PermissionRepository;
import com.vyoog.eisplatform.modules.authorization.repository.RoleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-KNW-008: only platform administrators and persons given a knowledge
 * permission may create, upload, publish or delete knowledge content; the
 * backend enforces it whatever the UI shows (BR-KPRM-001–004).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(KnowledgeTestConfig.class)
class KnowledgeAuthorizationTest {

    private static final String ARTICLE = """
        {"contentType":"ARTICLE","title":"Auth test article","blocks":[{"type":"paragraph","text":"Hello"}]}""";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void contributorRole() {
        if (roleRepository.findByName("KNOWLEDGE_WRITER").isEmpty()) {
            Permission contribute = permissionRepository.findByName("KNOWLEDGE_CONTRIBUTE").orElseThrow();
            Role role = new Role();
            role.setName("KNOWLEDGE_WRITER");
            role.setScope(RoleScope.PLATFORM);
            role.getPermissions().add(contribute);
            roleRepository.save(role);
        }
    }

    private static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("sub-admin")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor contributor() {
        return jwt().jwt(j -> j.subject("sub-writer")).authorities(new SimpleGrantedAuthority("ROLE_KNOWLEDGE_WRITER"));
    }

    private static RequestPostProcessor customer() {
        return jwt().jwt(j -> j.subject("sub-customer")).authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"));
    }

    @Test
    void readersCannotCreateUploadPublishOrDelete() throws Exception {
        mockMvc.perform(get("/admin/knowledge/content")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/admin/knowledge/content").with(customer()).contentType(MediaType.APPLICATION_JSON).content(ARTICLE))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/knowledge/media/upload-url").with(customer()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"a.pdf\",\"contentType\":\"application/pdf\",\"size\":10,\"kind\":\"DOCUMENT\"}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/knowledge/videos/upload-url").with(customer()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"a.mp4\",\"contentType\":\"video/mp4\",\"size\":10}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/knowledge/content/1/publish").with(customer())).andExpect(status().isForbidden());
        mockMvc.perform(delete("/admin/knowledge/content/1").with(customer())).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/knowledge/content").contentType(MediaType.APPLICATION_JSON).content(ARTICLE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void contributorDraftsAndSubmitsButCannotPublishOrDelete() throws Exception {
        String body = mockMvc.perform(post("/admin/knowledge/content").with(contributor())
                .contentType(MediaType.APPLICATION_JSON).content(ARTICLE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.workflowState").value("DRAFT"))
            .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(body.replaceAll("(?s).*?\"id\":(\\d+).*", "$1"));
        mockMvc.perform(get("/admin/knowledge/me").with(contributor()))
            .andExpect(jsonPath("$.contributor").value(true)).andExpect(jsonPath("$.publisher").value(false));
        mockMvc.perform(post("/admin/knowledge/content/" + id + "/submit").with(contributor()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.workflowState").value("IN_REVIEW"));
        mockMvc.perform(post("/admin/knowledge/content/" + id + "/approve").with(contributor()))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
        mockMvc.perform(post("/admin/knowledge/content/" + id + "/publish").with(contributor())).andExpect(status().isForbidden());
        mockMvc.perform(delete("/admin/knowledge/content/" + id).with(contributor())).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/knowledge/taxonomy/products").with(contributor()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"X\"}")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/knowledge/search-index").with(contributor())).andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/knowledge/content/" + id + "/approve").with(admin())).andExpect(status().isOk());
        mockMvc.perform(post("/admin/knowledge/content/" + id + "/publish").with(admin()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"versionBump\":\"MINOR\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.liveVersion").value("1.0"));

        // Readers, signed out, see it through the Knowledge Center and the old endpoint.
        mockMvc.perform(get("/knowledge/content/" + id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Auth test article"));
        mockMvc.perform(get("/knowledge-base/articles/" + id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PUBLISHED"));
        mockMvc.perform(delete("/admin/knowledge/content/" + id).with(admin())).andExpect(status().isNoContent());
        mockMvc.perform(get("/knowledge/content/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void knowledgeCenterIsPublicAndFeedbackNeedsSignIn() throws Exception {
        mockMvc.perform(get("/knowledge/home")).andExpect(status().isOk())
            .andExpect(jsonPath("$.products.length()").value(6));
        mockMvc.perform(get("/knowledge/glossary")).andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.term == 'OEE')]").exists());
        mockMvc.perform(get("/knowledge/workflows")).andExpect(status().isOk());
        mockMvc.perform(get("/knowledge/products/valam")).andExpect(status().isOk())
            .andExpect(jsonPath("$.modules.length()").value(11));
        mockMvc.perform(get("/knowledge/assistant/status")).andExpect(jsonPath("$.configured").value(false));
        mockMvc.perform(post("/knowledge/assistant/ask").contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"hi\"}"))
            .andExpect(status().isNotImplemented()).andExpect(jsonPath("$.code").value("ASSISTANT_NOT_CONFIGURED"));
        mockMvc.perform(post("/knowledge/content/1/feedback").contentType(MediaType.APPLICATION_JSON)
            .content("{\"kind\":\"VOTE\",\"helpful\":true}")).andExpect(status().isUnauthorized());
    }

    @Test
    void storageInformationNeverContainsCredentials() throws Exception {
        mockMvc.perform(get("/admin/knowledge/media/storage").with(admin()))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("secret"))))
            .andExpect(content().string(not(containsString("AccessKey"))));
    }
}
