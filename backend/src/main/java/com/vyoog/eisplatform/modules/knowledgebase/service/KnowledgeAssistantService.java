package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeReaderDtos;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * "Ask eVyoog Knowledge" (REQ-KNW-007, C75): prepared, not built. Until D8
 * (LLM provider) is decided, no model is ever called (BR-KAST-001): status
 * says not configured and ask answers 501 ASSISTANT_NOT_CONFIGURED.
 */
@Service
public class KnowledgeAssistantService {

    public KnowledgeReaderDtos.AssistantStatus status() {
        return new KnowledgeReaderDtos.AssistantStatus(false,
            "The AI assistant is coming soon. Search the Knowledge Center or contact support.");
    }

    public void ask(KnowledgeReaderDtos.AssistantRequest request) {
        throw new KnowledgeException(HttpStatus.NOT_IMPLEMENTED, "ASSISTANT_NOT_CONFIGURED",
            "The AI assistant is not available yet.");
    }
}
