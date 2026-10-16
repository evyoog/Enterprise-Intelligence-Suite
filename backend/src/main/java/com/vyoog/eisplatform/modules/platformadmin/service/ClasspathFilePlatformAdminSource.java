package com.vyoog.eisplatform.modules.platformadmin.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads the registry from the application classpath ({@code classpath:config/platform-admins.json}) or from an absolute file ({@code file:/path}). */
public class ClasspathFilePlatformAdminSource implements PlatformAdminSource {

    private final String scheme;
    private final String target;
    private final ClassLoader classLoader;

    public ClasspathFilePlatformAdminSource(String location) {
        this(location, ClasspathFilePlatformAdminSource.class.getClassLoader());
    }

    public ClasspathFilePlatformAdminSource(String location, ClassLoader classLoader) {
        this.classLoader = classLoader;
        if (location.startsWith("classpath:")) {
            this.scheme = "classpath";
            String path = location.substring("classpath:".length());
            this.target = path.startsWith("/") ? path.substring(1) : path;
        } else if (location.startsWith("file:")) {
            this.scheme = "file";
            String path = location.substring("file:".length());
            this.target = path.startsWith("//") ? path.substring(2) : path; // file:///abs/path and file:/abs/path
        } else {
            throw new IllegalArgumentException("Not a classpath: or file: location");
        }
    }

    @Override
    public String read() throws IOException {
        if ("file".equals(scheme)) {
            return Files.readString(Path.of(target), StandardCharsets.UTF_8);
        }
        try (InputStream in = classLoader.getResourceAsStream(target)) {
            if (in == null) {
                throw new IOException("Classpath resource not found");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public String scheme() {
        return scheme;
    }
}
