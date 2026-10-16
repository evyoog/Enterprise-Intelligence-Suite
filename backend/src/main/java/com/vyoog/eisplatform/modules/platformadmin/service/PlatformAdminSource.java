package com.vyoog.eisplatform.modules.platformadmin.service;

import java.io.IOException;

/** Where the platform admin registry JSON is read from (C108): the project (classpath or file) or S3. */
public interface PlatformAdminSource {

    /** The whole JSON text. May throw any exception when the source is unreachable; the registry keeps the last good copy. */
    String read() throws IOException;

    /** {@code classpath}, {@code file}, {@code s3} or {@code invalid}: the only thing ever logged about the source. */
    String scheme();
}
