package com.vyoog.eisplatform.modules.evaluation.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.evaluation.dto.EvaluationDtos;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** C102 / REQ-MKT-005 (03.02.01.02): a customer asks for a demo; it becomes a support ticket in the category "Demo request", so the support team and the SLA handle it. */
@Service
@RequiredArgsConstructor
public class DemoRequestService {

    public static final String CATEGORY = "Demo request";
    private static final int MAX_FIELD = 1000;

    private final ProductRepository productRepository;
    private final SupportTicketService ticketService;

    public SupportTicketDto request(Long customerId, EvaluationDtos.DemoRequest request) {
        if (request == null || request.productId() == null) {
            throw new IllegalArgumentException("Choose a product.");
        }
        Product product = productRepository.findById(request.productId()).filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        String time = clean(request.preferredTime());
        String message = clean(request.message());
        StringBuilder text = new StringBuilder("I would like a demo of ").append(product.getName()).append(" (product ").append(product.getId()).append(").");
        if (time != null) {
            text.append("\n\nPreferred time: ").append(time);
        }
        if (message != null) {
            text.append("\n\nMessage: ").append(message);
        }
        return ticketService.createTicket(customerId, new CreateTicketRequest(subject(product.getName()), text.toString()), CATEGORY);
    }

    private static String subject(String productName) {
        String subject = "Demo request: " + productName;
        return subject.length() <= 200 ? subject : subject.substring(0, 200);
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String t = value.trim();
        if (t.length() > MAX_FIELD) {
            throw new IllegalArgumentException("Keep each field under " + MAX_FIELD + " characters.");
        }
        return t;
    }
}
