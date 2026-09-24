package com.vyoog.eisplatform.modules.product.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded product images on local disk (app.file-upload-dir) — fine for a
 * single-instance deployment; would need a shared store (e.g. S3) behind a load
 * balancer with multiple instances. Filenames are never trusted from the client:
 * generated as a random UUID + a whitelisted extension, so path traversal
 * ("../../etc/passwd") and executable-disguised-as-image uploads aren't possible.
 */
@Service
public class ProductImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final Path uploadDir;

    public ProductImageService(@Value("${app.file-upload-dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public record LoadedImage(Resource resource, MediaType contentType) {
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        String originalName = StringUtils.cleanPath(
            file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = extensionOf(originalName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported image type: " + extension);
        }

        try {
            Files.createDirectories(uploadDir);
            String storedFilename = UUID.randomUUID() + extension;
            file.transferTo(uploadDir.resolve(storedFilename));
            return storedFilename;
        } catch (IOException e) {
            throw new IllegalStateException("Could not store the uploaded file", e);
        }
    }

    public LoadedImage load(String filename) {
        // Belt-and-braces: reject anything that isn't a bare filename before it
        // ever reaches path resolution, then re-check after normalizing too.
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResourceNotFoundException("Image not found: " + filename);
        }

        Path filePath = uploadDir.resolve(filename).normalize();
        if (!filePath.startsWith(uploadDir)) {
            throw new ResourceNotFoundException("Image not found: " + filename);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Image not found: " + filename);
            }
            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = contentType != null
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;
            return new LoadedImage(resource, mediaType);
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Image not found: " + filename);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the image file", e);
        }
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
