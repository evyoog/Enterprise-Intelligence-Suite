package com.vyoog.eisplatform.modules.wishlist.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.wishlist.model.WishlistItem;
import com.vyoog.eisplatform.modules.wishlist.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * C94 (REQ-MKT-004): the signed-in customer's saved products. Saving is idempotent and only an ACTIVE product can be
 * saved (BR-2); a saved product that later stops being ACTIVE stays in the list but is not shown in the catalog.
 * Wishlist changes are shopping activity and are not audited, like cart changes (REQ-MKT-003.9).
 */
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository repository;
    private final ProductRepository productRepository;

    /** Product ids, most recently saved first. */
    @Transactional(readOnly = true)
    public List<Long> list(Long customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(WishlistItem::getProductId).toList();
    }

    @Transactional
    public List<Long> add(Long customerId, Long productId) {
        productRepository.findById(productId)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!repository.existsByCustomerIdAndProductId(customerId, productId)) {
            WishlistItem item = new WishlistItem();
            item.setCustomerId(customerId);
            item.setProductId(productId);
            repository.save(item);
        }
        return list(customerId);
    }

    @Transactional
    public List<Long> remove(Long customerId, Long productId) {
        repository.deleteByCustomerIdAndProductId(customerId, productId);
        return list(customerId);
    }
}
