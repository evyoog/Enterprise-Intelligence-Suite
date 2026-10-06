package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeReaderDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeSummaryDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeBookmark;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Personal learning (REQ-KNW-005.8): bookmarks, recently viewed and progress.
 * Per user and never shown to anyone else (BR-KCEN-004); only content the
 * reader may still see is listed.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgePersonalService {

    private final KnowledgeBookmarkRepository bookmarkRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeViewService viewService;
    private final KnowledgeAnalyticsService analyticsService;

    public KnowledgeReaderDtos.Personal personal(KnowledgeReader reader) {
        return analyticsService.personal(reader, bookmarked(reader));
    }

    public List<KnowledgeSummaryDto> bookmarks(KnowledgeReader reader) {
        return viewService.summaries(bookmarked(reader));
    }

    private List<KnowledgeViewService.Live> bookmarked(KnowledgeReader reader) {
        if (reader.customerId() == null) {
            return List.of();
        }
        List<KnowledgeBookmark> marks = bookmarkRepository.findByCustomerIdOrderByCreatedAtDesc(reader.customerId());
        Map<Long, KnowledgeArticle> items = articleRepository.findAllById(marks.stream().map(KnowledgeBookmark::getContentId).toList())
            .stream().collect(Collectors.toMap(KnowledgeArticle::getId, Function.identity()));
        Map<Long, KnowledgeViewService.Live> visible = viewService.filterVisible(List.copyOf(items.values()), reader).stream()
            .collect(Collectors.toMap(l -> l.item().getId(), Function.identity()));
        return marks.stream().map(m -> visible.get(m.getContentId())).filter(java.util.Objects::nonNull).toList();
    }

    @Transactional
    public void addBookmark(KnowledgeReader reader, Long contentId) {
        requireCustomer(reader);
        KnowledgeArticle item = articleRepository.findById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found"));
        if (viewService.visibleItem(item, reader).isEmpty()) {
            throw new ResourceNotFoundException("Content not found");
        }
        if (bookmarkRepository.findByCustomerIdAndContentId(reader.customerId(), contentId).isEmpty()) {
            KnowledgeBookmark mark = new KnowledgeBookmark();
            mark.setCustomerId(reader.customerId());
            mark.setContentId(contentId);
            bookmarkRepository.save(mark);
        }
    }

    @Transactional
    public void removeBookmark(KnowledgeReader reader, Long contentId) {
        requireCustomer(reader);
        bookmarkRepository.findByCustomerIdAndContentId(reader.customerId(), contentId).ifPresent(bookmarkRepository::delete);
    }

    @Transactional
    public void saveProgress(KnowledgeReader reader, Long contentId, KnowledgeReaderDtos.ProgressRequest request) {
        analyticsService.saveProgress(reader, contentId, request.percent(), request.positionSeconds());
    }

    private static void requireCustomer(KnowledgeReader reader) {
        if (reader.customerId() == null) {
            throw new KnowledgeException(HttpStatus.UNAUTHORIZED, "SIGN_IN_REQUIRED",
                "Sign in with a customer account to save items.");
        }
    }
}
