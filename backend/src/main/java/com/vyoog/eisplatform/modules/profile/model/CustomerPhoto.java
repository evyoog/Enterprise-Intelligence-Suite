package com.vyoog.eisplatform.modules.profile.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A person's profile photo (C107), one row per customer, kept in the database on purpose (the object
 * storage decision KC-3 is still open). Deliberately NOT a field of the Customer entity, so the bytes are
 * only ever read when the photo itself is requested; every other read uses {@link
 * com.vyoog.eisplatform.modules.profile.repository.CustomerPhotoRepository#findMetaByCustomerId}.
 */
@Entity
@Table(name = "customer_photo")
@Getter
@Setter
public class CustomerPhoto {

    /** Same value as customer.id (primary key and foreign key, deleted with the customer). */
    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "content_type", nullable = false, length = 40)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    /** At most {@link com.vyoog.eisplatform.modules.profile.service.ProfilePhotoInspector#MAX_BYTES} bytes. */
    @Column(nullable = false, length = 1_048_576)
    private byte[] data;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
