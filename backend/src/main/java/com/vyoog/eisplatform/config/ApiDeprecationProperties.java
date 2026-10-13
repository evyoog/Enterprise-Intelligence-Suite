package com.vyoog.eisplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** {@code app.api.deprecations[n].path-prefix / sunset / successor} (REQ-INT-001, C92, API-5 B). Empty today. */
@Component
@ConfigurationProperties(prefix = "app.api")
public class ApiDeprecationProperties {

    public static class Deprecation {
        private String pathPrefix;
        private LocalDate sunset;
        private String successor;

        public String getPathPrefix() { return pathPrefix; }
        public void setPathPrefix(String pathPrefix) { this.pathPrefix = pathPrefix; }
        public LocalDate getSunset() { return sunset; }
        public void setSunset(LocalDate sunset) { this.sunset = sunset; }
        public String getSuccessor() { return successor; }
        public void setSuccessor(String successor) { this.successor = successor; }
    }

    private List<Deprecation> deprecations = new ArrayList<>();

    public List<Deprecation> getDeprecations() { return deprecations; }
    public void setDeprecations(List<Deprecation> deprecations) { this.deprecations = deprecations; }
}
