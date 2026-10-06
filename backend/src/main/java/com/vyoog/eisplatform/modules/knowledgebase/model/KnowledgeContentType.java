package com.vyoog.eisplatform.modules.knowledgebase.model;

/** REQ-KNW-002 content types (C71–C77, defaults applied 2026-10-05). Existing
 * articles are ARTICLE (REQ-KNW-001.7). COURSE is prepared for the Academy
 * (11b) and not offered in the editor until the Academy is confirmed. */
public enum KnowledgeContentType {
    ARTICLE,
    GETTING_STARTED,
    PRODUCT_GUIDE,
    VIDEO,
    DOCUMENT,
    TEMPLATE,
    STUDY_MATERIAL,
    RELEASE_NOTE,
    FAQ,
    TROUBLESHOOTING,
    ERROR_CODE,
    GLOSSARY_TERM,
    WORKFLOW_GUIDE,
    DEVELOPER_DOC,
    COURSE
}
