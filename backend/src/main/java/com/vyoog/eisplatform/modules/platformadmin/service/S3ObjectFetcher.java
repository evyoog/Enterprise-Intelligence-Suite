package com.vyoog.eisplatform.modules.platformadmin.service;

import java.io.IOException;

/** The one call the S3 source needs, so tests can replace AWS with a lambda. */
@FunctionalInterface
public interface S3ObjectFetcher {

    /** The object's content as UTF-8 text. */
    String fetch(String bucket, String key) throws IOException;
}
