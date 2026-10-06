package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeCategory;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeModule;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProduct;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeCategoryRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Knowledge Center start-up work:
 * <ol>
 *   <li>REQ-KNW-001.7 — existing articles become ARTICLE items: one paragraph
 *       block holding the body, and a live version "{version}.0" for
 *       published ones. Idempotent (the migration V021 does the same in SQL).</li>
 *   <li>C74 seed data, once, when no knowledge product exists: the six
 *       products and their modules from the Knowledge Center requirement,
 *       troubleshooting/video/document categories, the glossary terms and
 *       workflow guides listed there (published), and the template library
 *       list as drafts without files (publishers attach the files). All of it
 *       is ordinary data that publishers edit; nothing is hard-coded in the UI.</li>
 * </ol>
 */
@Component
@Order(50)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeSeeder implements ApplicationRunner {

    static final Map<String, List<String>> PRODUCTS = new LinkedHashMap<>();

    static {
        PRODUCTS.put("Valam.ai", List.of("HR", "Finance", "Inventory", "Assets", "Payroll", "Attendance", "Product Master",
            "Item Master", "BOM", "Tax", "Reports"));
        PRODUCTS.put("Varthan.ai", List.of("Sales", "Purchase", "Procurement", "Shipping", "Stock", "Orders", "Invoices",
            "Vendors", "Customers"));
        PRODUCTS.put("Yukth.ai", List.of("Customers", "Vendors", "Partners", "Consultants", "Contracts", "Legal",
            "Service Requests", "SLA", "Warranty"));
        PRODUCTS.put("Thittam.ai", List.of("Production Planning", "MRP", "Projects", "Demand Planning", "Capacity Planning",
            "Supply Planning", "S&OP", "Resource Planning"));
        PRODUCTS.put("Thiran.ai", List.of("Production", "MES", "Work Orders", "Quality", "QMS", "Maintenance", "OEE",
            "Warehouse", "Logistics", "Engineering", "Testing", "Field Service"));
        PRODUCTS.put("Tharav.ai", List.of("Dashboards", "Reports", "BI", "Data", "Data Catalog", "Data Quality", "Analytics",
            "Forecasting", "Anomaly Detection", "AI", "Natural Language Query", "Enterprise Search", "RAG"));
    }

    static final List<String> TROUBLESHOOTING = List.of("Login", "Permissions", "Product access", "Configuration",
        "Transaction errors", "Integration", "Data", "Reports", "Performance");
    static final List<String> VIDEO_KINDS = List.of("Product tutorial", "Feature tutorial", "Getting started",
        "Troubleshooting", "Training", "Webinar", "Product demonstration");
    static final List<String> DOCUMENT_KINDS = List.of("User manual", "Implementation guide", "Configuration guide",
        "Brochure", "Checklist", "Sample document", "API documentation");

    /** Glossary (REQ-KNW-005.21 seed): term → meaning, synonyms. */
    static final List<String[]> GLOSSARY = List.of(
        new String[] {"BOM", "Bill of materials: the list of raw materials, components and quantities needed to make one unit of a product.", "Bill of materials"},
        new String[] {"MRP", "Material requirements planning: calculates which materials are needed, how many and when, from demand, stock and the bill of materials.", "Material requirements planning"},
        new String[] {"OEE", "Overall equipment effectiveness: availability × performance × quality, the share of planned production time that is truly productive.", "Overall equipment effectiveness"},
        new String[] {"WIP", "Work in progress: materials and products that have entered production but are not finished yet.", "Work in process"},
        new String[] {"S&OP", "Sales and operations planning: a regular process that aligns demand, supply and financial plans.", "Sales and operations planning"},
        new String[] {"MES", "Manufacturing execution system: tracks and controls production on the shop floor in real time.", "Manufacturing execution system"},
        new String[] {"QMS", "Quality management system: the processes and records used to meet quality requirements.", "Quality management system"},
        new String[] {"CAPA", "Corrective and preventive action: fixing the cause of a problem and preventing it from happening again.", "Corrective and preventive action"},
        new String[] {"RFQ", "Request for quotation: a request asking suppliers for prices and terms for specified goods or services.", "Request for quotation"},
        new String[] {"MOQ", "Minimum order quantity: the smallest quantity a supplier accepts in one order.", "Minimum order quantity"},
        new String[] {"EOQ", "Economic order quantity: the order size that minimises the total of ordering and holding costs.", "Economic order quantity"});

    /** Workflow guides (REQ-KNW-005.14 seed). */
    static final Map<String, List<String>> WORKFLOWS = new LinkedHashMap<>();

    static {
        WORKFLOWS.put("Procure-to-pay", List.of("Purchase request", "RFQ", "Vendor quotation", "Comparison", "Purchase order",
            "Goods receipt", "Inspection", "Invoice", "Payment"));
        WORKFLOWS.put("Lead-to-cash", List.of("Lead", "Opportunity", "Quotation", "Sales order", "Delivery", "Invoice", "Payment"));
        WORKFLOWS.put("Production", List.of("Demand", "Planning", "MRP", "Work order", "Production", "Quality", "Inventory",
            "Reporting"));
    }

    /** Template library (REQ-KNW-005.18 seed) — drafts until a file is attached. */
    static final List<String> TEMPLATES = List.of("Purchase order", "RFQ", "Employee import", "Attendance import",
        "Chart of accounts", "Opening balance", "BOM", "Routing");

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeProductRepository productRepository;
    private final KnowledgeModuleRepository moduleRepository;
    private final KnowledgeCategoryRepository categoryRepository;
    private final KnowledgeContentService contentService;
    private final KnowledgeSettings settings;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        upgradeExistingArticles();
        if (settings.isSeedEnabled() && productRepository.count() == 0) {
            seed();
        }
    }

    /** REQ-KNW-001.7 / BR-KNW-007: ids, title, body, status and version unchanged. */
    @Transactional
    public int upgradeExistingArticles() {
        int upgraded = 0;
        for (KnowledgeArticle a : articleRepository.findAll()) {
            boolean changed = false;
            if (a.getBlocks() == null) {
                a.setBlocks(KnowledgeBlocks.paragraph(a.getBody()));
                changed = true;
            }
            if (a.getSlug() == null) {
                a.setSlug(KnowledgeContentService.slugify(a.getTitle()) + "-" + a.getId());
                changed = true;
            }
            if (a.getSearchText() == null) {
                contentService.refreshSearchText(a);
                changed = true;
            }
            if (a.getStatus() == ArticleStatus.PUBLISHED && a.getLiveVersionId() == null) {
                var v = contentService.snapshot(a, a.getVersion() + ".0", null);
                a.setLiveVersionId(v.getId());
                a.setCurrentVersionLabel(v.getVersionLabel());
                a.setWorkflowState(KnowledgeWorkflowState.PUBLISHED);
                a.setPublishedAt(a.getUpdatedAt() != null ? a.getUpdatedAt() : v.getPublishedAt());
                changed = true;
            }
            if (changed) {
                articleRepository.save(a);
                upgraded++;
            }
        }
        if (upgraded > 0) {
            log.info("Knowledge Center: {} existing article(s) moved to the content model", upgraded);
        }
        return upgraded;
    }

    private void seed() {
        int order = 0;
        for (Map.Entry<String, List<String>> entry : PRODUCTS.entrySet()) {
            KnowledgeProduct p = new KnowledgeProduct();
            p.setName(entry.getKey());
            p.setSlug(KnowledgeContentService.slugify(entry.getKey().replace(".ai", "")));
            p.setDisplayOrder(order++);
            p.setCatalogProductId(productRepository.findCatalogProductIdByName(entry.getKey()).orElse(null));
            p = productRepository.save(p);
            int m = 0;
            for (String name : entry.getValue()) {
                KnowledgeModule module = new KnowledgeModule();
                module.setProductId(p.getId());
                module.setName(name);
                module.setSlug(KnowledgeContentService.slugify(name.replace("&", " and ")));
                module.setDisplayOrder(m++);
                moduleRepository.save(module);
            }
        }
        category("General", null, 0);
        seedCategories(TROUBLESHOOTING, KnowledgeContentType.TROUBLESHOOTING);
        seedCategories(VIDEO_KINDS, KnowledgeContentType.VIDEO);
        seedCategories(DOCUMENT_KINDS, KnowledgeContentType.DOCUMENT);

        for (String[] term : GLOSSARY) {
            ObjectNode fields = JsonNodeFactory.instance.objectNode();
            fields.put("term", term[0]);
            fields.put("definition", term[1]);
            fields.putArray("synonyms").add(term[2]);
            KnowledgeArticle a = item(KnowledgeContentType.GLOSSARY_TERM, term[0], term[2], KnowledgeBlocks.paragraph(term[1]),
                fields.toString());
            contentService.publishNow(a, false, null);
        }
        for (Map.Entry<String, List<String>> wf : WORKFLOWS.entrySet()) {
            ArrayNode blocks = JsonNodeFactory.instance.arrayNode();
            ObjectNode diagram = blocks.addObject();
            diagram.put("type", "workflow_diagram");
            diagram.put("title", wf.getKey());
            ArrayNode steps = diagram.putArray("steps");
            wf.getValue().forEach(step -> steps.addObject().put("title", step));
            KnowledgeArticle a = item(KnowledgeContentType.WORKFLOW_GUIDE, wf.getKey(),
                String.join(" → ", wf.getValue()), blocks.toString(), null);
            contentService.publishNow(a, false, null);
        }
        for (String template : TEMPLATES) {
            item(KnowledgeContentType.TEMPLATE, template + " template", null, "[]", null);
        }
        log.info("Knowledge Center: seed data created (C74)");
    }

    private void seedCategories(List<String> names, KnowledgeContentType scope) {
        int i = 0;
        for (String name : names) {
            category(name, scope, i++);
        }
    }

    private void category(String name, KnowledgeContentType scope, int order) {
        KnowledgeCategory c = new KnowledgeCategory();
        c.setName(name);
        c.setSlug(KnowledgeContentService.slugify(name));
        c.setScope(scope);
        c.setDisplayOrder(order);
        categoryRepository.save(c);
    }

    private KnowledgeArticle item(KnowledgeContentType type, String title, String shortDescription, String blocks,
                                  String typeFields) {
        KnowledgeArticle a = new KnowledgeArticle();
        a.setContentType(type);
        a.setTitle(title);
        a.setShortDescription(shortDescription);
        a.setBlocks(blocks);
        a.setTypeFields(typeFields);
        String text = KnowledgeBlocks.plainText(blocks);
        a.setBody(text.isBlank() ? (shortDescription == null ? title : shortDescription) : text);
        a.setAudience(KnowledgeAudience.PUBLIC);
        a.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        a = articleRepository.save(a);
        a.setSlug(KnowledgeContentService.slugify(title.replace("&", " and ")));
        contentService.refreshSearchText(a);
        return articleRepository.save(a);
    }
}
