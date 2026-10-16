package com.vyoog.eisplatform.modules.platformadmin.service;

import java.io.IOException;

/** Chooses the registry source from the configured location (C108). */
public final class PlatformAdminSourceFactory {

    private PlatformAdminSourceFactory() {
    }

    public static PlatformAdminSource create(PlatformAdminProperties properties) {
        return create(properties.getLocation(), new AwsS3ObjectFetcher(properties.getS3Region(), properties.getS3Endpoint()));
    }

    /**
     * An unsupported or malformed location does not stop the application: it yields a source that always
     * fails, so the registry stays empty (fail closed) and the problem is logged by the registry.
     */
    public static PlatformAdminSource create(String location, S3ObjectFetcher s3) {
        try {
            if (location.startsWith("s3://")) {
                return new S3PlatformAdminSource(location, s3);
            }
            return new ClasspathFilePlatformAdminSource(location);
        } catch (IllegalArgumentException e) {
            return new PlatformAdminSource() {
                @Override
                public String read() throws IOException {
                    throw new IOException("Unsupported platform admin location");
                }

                @Override
                public String scheme() {
                    return "invalid";
                }
            };
        }
    }
}
