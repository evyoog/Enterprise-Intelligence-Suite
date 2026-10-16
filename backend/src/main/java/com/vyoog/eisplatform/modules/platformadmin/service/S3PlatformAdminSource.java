package com.vyoog.eisplatform.modules.platformadmin.service;

import java.io.IOException;

/** Reads the registry from {@code s3://bucket/key}. */
public class S3PlatformAdminSource implements PlatformAdminSource {

    private final String bucket;
    private final String key;
    private final S3ObjectFetcher fetcher;

    public S3PlatformAdminSource(String location, S3ObjectFetcher fetcher) {
        String rest = location == null || !location.startsWith("s3://") ? null : location.substring("s3://".length());
        int slash = rest == null ? -1 : rest.indexOf('/');
        if (slash <= 0 || slash == rest.length() - 1) {
            throw new IllegalArgumentException("An S3 location must look like s3://bucket/key");
        }
        this.bucket = rest.substring(0, slash);
        this.key = rest.substring(slash + 1);
        this.fetcher = fetcher;
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    @Override
    public String read() throws IOException {
        return fetcher.fetch(bucket, key);
    }

    @Override
    public String scheme() {
        return "s3";
    }
}
