package com.vyoog.eisplatform.modules.platformadmin.service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Real S3 read for the platform admin registry (C108). Credentials are only ever the AWS default provider
 * chain (an ECS task or instance role): no key is accepted from configuration. The region is optional (the
 * SDK's own region chain applies when blank); an endpoint is only for MinIO or LocalStack.
 */
public class AwsS3ObjectFetcher implements S3ObjectFetcher {

    private final String region;
    private final String endpoint;
    private volatile S3Client client;

    public AwsS3ObjectFetcher(String region, String endpoint) {
        this.region = region == null ? "" : region.strip();
        this.endpoint = endpoint == null ? "" : endpoint.strip();
    }

    @Override
    public String fetch(String bucket, String key) {
        return client().getObjectAsBytes(b -> b.bucket(bucket).key(key)).asUtf8String();
    }

    private S3Client client() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    var builder = S3Client.builder().credentialsProvider(DefaultCredentialsProvider.create());
                    if (!region.isBlank()) {
                        builder.region(Region.of(region));
                    }
                    if (!endpoint.isBlank()) {
                        builder.endpointOverride(URI.create(endpoint))
                            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
                    }
                    client = builder.build();
                }
            }
        }
        return client;
    }
}
