package com.vyoog.eisplatform.modules.profile.repository;

import com.vyoog.eisplatform.modules.profile.model.CustomerPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface CustomerPhotoRepository extends JpaRepository<CustomerPhoto, Long> {

    /** What the profile needs to know about a photo without loading its bytes. */
    interface PhotoMeta {
        String getContentType();

        Integer getSizeBytes();

        Instant getUpdatedAt();
    }

    @Query("select p.contentType as contentType, p.sizeBytes as sizeBytes, p.updatedAt as updatedAt "
        + "from CustomerPhoto p where p.customerId = :customerId")
    Optional<PhotoMeta> findMetaByCustomerId(@Param("customerId") Long customerId);
}
