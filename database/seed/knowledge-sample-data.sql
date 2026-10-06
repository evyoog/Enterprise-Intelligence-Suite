-- Knowledge Center sample data: Manufacturing ERP content (incl. Vyoog / eVyoog videos), YouTube videos with thumbnails,
-- and demo reader activity. Idempotent (rows are keyed by slug / unique keys; re-running adds nothing).
-- Videos are real public YouTube videos (ids verified via YouTube oEmbed on 2026-10-06); the player embeds
-- them from youtube-nocookie.com and the thumbnail is https://i.ytimg.com/vi/<id>/hqdefault.jpg.
-- Taxonomy: uses the existing Knowledge Center products and modules (Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai).
-- Nine videos are from the eVyoog / Vyoog walkthrough series on YouTube (channel "Nagaraj Samiyappan"; confirm it is
-- an official Vyoog channel before showing to customers); the rest are third-party manufacturing ERP explainers.
-- Demo only. Never run against production. Run: psql -v ON_ERROR_STOP=1 -f knowledge-sample-data.sql
SET search_path TO eis_platform, public;
BEGIN;


-- 2. Content items (videos first, so articles can embed them)
INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-introduction', 'Vyoog introduction', 'Introduction to Vyoog and eVyoog, the cloud ERP for machine shops, foundries, textile and other manufacturing industries.', 'A short introduction to Vyoog and the eVyoog software for manufacturing industries.', '[{"type": "paragraph", "text": "Introduction to Vyoog and eVyoog, the cloud ERP for machine shops, foundries, textile and other manufacturing industries."}]', NULL,
  'PUBLISHED', 1, NULL, NULL, (SELECT id FROM knowledge_category WHERE slug='getting-started' AND scope = 'VIDEO'),
  NULL, 'vyoog,evyoog,introduction,manufacturing erp', 'vyoog evyoog introduction manufacturing erp', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), TRUE, 'BEGINNER', NULL, NULL, 'Vyoog introduction Nagaraj Samiyappan vyoog evyoog introduction manufacturing erp',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-introduction');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-enquiry', 'eVyoog - Enquiry', 'How to record and follow up a customer enquiry in eVyoog, the first step of the order-to-cash flow.', 'Record a customer enquiry in eVyoog.', '[{"type": "paragraph", "text": "How to record and follow up a customer enquiry in eVyoog, the first step of the order-to-cash flow."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='varthan'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='varthan' AND km.slug='sales'), (SELECT id FROM knowledge_category WHERE slug='product-tutorial' AND scope = 'VIDEO'),
  NULL, 'evyoog,enquiry,sales,order to cash', 'evyoog enquiry sales customer', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'eVyoog - Enquiry Nagaraj Samiyappan evyoog enquiry sales order to cash',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-enquiry');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-lead', 'eVyoog - Lead', 'How to capture and qualify a sales lead in eVyoog.', 'Capture and qualify a sales lead in eVyoog.', '[{"type": "paragraph", "text": "How to capture and qualify a sales lead in eVyoog."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='varthan'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='varthan' AND km.slug='sales'), (SELECT id FROM knowledge_category WHERE slug='product-tutorial' AND scope = 'VIDEO'),
  NULL, 'evyoog,lead,sales', 'evyoog lead sales crm', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'eVyoog - Lead Nagaraj Samiyappan evyoog lead sales',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-lead');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-quotation', 'eVyoog - Quotation', 'How to prepare and send a customer quotation in eVyoog.', 'Prepare a quotation for a customer in eVyoog.', '[{"type": "paragraph", "text": "How to prepare and send a customer quotation in eVyoog."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='varthan'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='varthan' AND km.slug='sales'), (SELECT id FROM knowledge_category WHERE slug='product-tutorial' AND scope = 'VIDEO'),
  NULL, 'evyoog,quotation,sales', 'evyoog quotation price customer', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'eVyoog - Quotation Nagaraj Samiyappan evyoog quotation sales',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-quotation');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-order-booking', 'eVyoog - OrderBooking', 'How to convert a quotation into a booked customer order in eVyoog.', 'Book a customer order in eVyoog.', '[{"type": "paragraph", "text": "How to convert a quotation into a booked customer order in eVyoog."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='varthan'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='varthan' AND km.slug='orders'), (SELECT id FROM knowledge_category WHERE slug='product-tutorial' AND scope = 'VIDEO'),
  NULL, 'evyoog,order booking,orders', 'evyoog order booking sales order', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), TRUE, 'BEGINNER', NULL, NULL, 'eVyoog - OrderBooking Nagaraj Samiyappan evyoog order booking orders',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-order-booking');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-inward', 'eVyoog - Inward', 'How to record the inward receipt of purchased material against a purchase order in eVyoog.', 'Receive incoming material in eVyoog.', '[{"type": "paragraph", "text": "How to record the inward receipt of purchased material against a purchase order in eVyoog."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='varthan'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='varthan' AND km.slug='stock'), (SELECT id FROM knowledge_category WHERE slug='product-tutorial' AND scope = 'VIDEO'),
  NULL, 'evyoog,inward,goods receipt,stock', 'evyoog inward goods receipt stock', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'eVyoog - Inward Nagaraj Samiyappan evyoog inward goods receipt stock',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-inward');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-inward-inspection', 'eVyoog-Inward Inspection', 'How to inspect incoming material and accept or reject it in eVyoog before it enters stock.', 'Inspect incoming material before it enters stock.', '[{"type": "paragraph", "text": "How to inspect incoming material and accept or reject it in eVyoog before it enters stock."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='quality'), (SELECT id FROM knowledge_category WHERE slug='feature-tutorial' AND scope = 'VIDEO'),
  NULL, 'evyoog,inward inspection,quality,incoming', 'evyoog inward inspection quality incoming material', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'INTERMEDIATE', NULL, NULL, 'eVyoog-Inward Inspection Nagaraj Samiyappan evyoog inward inspection quality incoming',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-inward-inspection');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-invoice', 'eVyoog - Invoice', 'How to raise a customer invoice from a delivered order in eVyoog.', 'Raise a customer invoice in eVyoog.', '[{"type": "paragraph", "text": "How to raise a customer invoice from a delivered order in eVyoog."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='varthan'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='varthan' AND km.slug='invoices'), (SELECT id FROM knowledge_category WHERE slug='product-tutorial' AND scope = 'VIDEO'),
  NULL, 'evyoog,invoice,billing', 'evyoog invoice billing customer', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'eVyoog - Invoice Nagaraj Samiyappan evyoog invoice billing',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-invoice');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'evyoog-payroll', 'eVyoog - Payroll', 'How to run monthly payroll for your employees in eVyoog.', 'Run payroll in eVyoog.', '[{"type": "paragraph", "text": "How to run monthly payroll for your employees in eVyoog."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='valam'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='valam' AND km.slug='payroll'), (SELECT id FROM knowledge_category WHERE slug='product-tutorial' AND scope = 'VIDEO'),
  NULL, 'evyoog,payroll,hr', 'evyoog payroll salary employees', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'INTERMEDIATE', NULL, NULL, 'eVyoog - Payroll Nagaraj Samiyappan evyoog payroll hr',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-payroll');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'bom-explained-manufacturing-erp', 'Bill of Materials (BOM) explained for manufacturing | ERP overview with Acumatica', 'Bill of materials explained for manufacturing: levels, components, routings and how an ERP uses the BOM to plan and cost production.', 'What a bill of materials is and how an ERP uses it in manufacturing.', '[{"type": "paragraph", "text": "Bill of materials explained for manufacturing: levels, components, routings and how an ERP uses the BOM to plan and cost production."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='valam'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='valam' AND km.slug='bom'), (SELECT id FROM knowledge_category WHERE slug='feature-tutorial' AND scope = 'VIDEO'),
  NULL, 'bom,bill of materials,manufacturing erp', 'bill of materials bom manufacturing erp', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'Bill of Materials (BOM) explained for manufacturing | ERP overview with Acumatica ERP Software Experts bom bill of materials manufacturing erp',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='bom-explained-manufacturing-erp');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'mrp-process-in-8-minutes', 'What is material requirement planning (MRP)? MRP process in 8 minutes', 'Material requirements planning in eight minutes: inputs, the MRP run and the planned orders it produces.', 'How MRP turns demand, stock and the bill of materials into purchase and production plans.', '[{"type": "paragraph", "text": "Material requirements planning in eight minutes: inputs, the MRP run and the planned orders it produces."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thittam'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thittam' AND km.slug='mrp'), (SELECT id FROM knowledge_category WHERE slug='training' AND scope = 'VIDEO'),
  NULL, 'mrp,planning,materials,manufacturing', 'mrp material requirement planning process', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'INTERMEDIATE', NULL, NULL, 'What is material requirement planning (MRP)? MRP process in 8 minutes Educationleaves mrp planning materials manufacturing',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='mrp-process-in-8-minutes');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'incoming-quality-control-manufacturing-erp', 'Incoming quality control in manufacturing ERP: step-by-step guide', 'Step-by-step incoming quality control in a manufacturing ERP: inspection plans, sampling, accept and reject decisions.', 'A step-by-step guide to incoming quality control in a manufacturing ERP.', '[{"type": "paragraph", "text": "Step-by-step incoming quality control in a manufacturing ERP: inspection plans, sampling, accept and reject decisions."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='quality'), (SELECT id FROM knowledge_category WHERE slug='training' AND scope = 'VIDEO'),
  NULL, 'quality control,incoming inspection,manufacturing erp', 'incoming quality control inspection manufacturing erp', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'INTERMEDIATE', NULL, NULL, 'Incoming quality control in manufacturing ERP: step-by-step guide Lighthouse Alchemy ERP Academy quality control incoming inspection manufacturing erp',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='incoming-quality-control-manufacturing-erp');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'rootstock-production-management-demo', 'Rootstock production management product demo', 'Demo of production management: work orders, material issue, shop-floor reporting and costing.', 'A product demo of production management in a manufacturing ERP.', '[{"type": "paragraph", "text": "Demo of production management: work orders, material issue, shop-floor reporting and costing."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='production'), (SELECT id FROM knowledge_category WHERE slug='product-demonstration' AND scope = 'VIDEO'),
  NULL, 'production management,work orders,demo', 'production management work orders shop floor demo', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'INTERMEDIATE', NULL, NULL, 'Rootstock production management product demo Rootstock Software production management work orders demo',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='rootstock-production-management-demo');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'VIDEO', 'odoo-16-manufacturing-module-demo', 'Odoo 16 manufacturing module demo', 'A 25-minute demo of an ERP manufacturing module: bills of materials, manufacturing orders, work centers and routings.', 'A long demo of the manufacturing module of an open-source ERP.', '[{"type": "paragraph", "text": "A 25-minute demo of an ERP manufacturing module: bills of materials, manufacturing orders, work centers and routings."}]', NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='production'), (SELECT id FROM knowledge_category WHERE slug='webinar' AND scope = 'VIDEO'),
  NULL, 'manufacturing orders,work centers,routings,erp demo', 'manufacturing module demo work centers routings', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'ADVANCED', NULL, NULL, 'Odoo 16 manufacturing module demo Cybrosys Technologies manufacturing orders work centers routings erp demo',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='VIDEO' AND slug='odoo-16-manufacturing-module-demo');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'GETTING_STARTED', 'getting-started-manufacturing-erp', 'Getting started with Manufacturing ERP', 'Before you begin
The manufacturing suite covers sales, purchase, stores, production, quality and accounts for machine shops, foundries, textile units and other manufacturers. Plan about a day for the first setup.
Create the item master: raw materials, components and finished goods.
Define units of measure and stores.
Build the bill of materials for each finished item.
Set up work centers and routings.
Add suppliers and customers.
Invite users and assign roles.
Tip
Create the item master before the bills of material: every BOM line points to an item.
Watch: an introduction to Vyoog and eVyoog
Open Products', 'Set up items, bills of material, work centers and users in your first week.', ('[{"type": "heading", "text": "Before you begin", "level": 2}, {"type": "paragraph", "text": "The manufacturing suite covers sales, purchase, stores, production, quality and accounts for machine shops, foundries, textile units and other manufacturers. Plan about a day for the first setup."}, {"type": "numbered_list", "items": ["Create the item master: raw materials, components and finished goods.", "Define units of measure and stores.", "Build the bill of materials for each finished item.", "Set up work centers and routings.", "Add suppliers and customers.", "Invite users and assign roles."]}, {"type": "note", "title": "Tip", "text": "Create the item master before the bills of material: every BOM line points to an item."}, {"type": "video", "contentId": ' || (SELECT id FROM knowledge_article WHERE content_type='VIDEO' AND slug='evyoog-introduction') || ', "caption": "Watch: an introduction to Vyoog and eVyoog"}, {"type": "button", "label": "Open Products", "url": "/products"}]'), NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='valam'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='valam' AND km.slug='bom'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'manufacturing erp,setup,items,bom,onboarding', 'getting started manufacturing erp setup items bom work centers', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), TRUE, 'BEGINNER', '/products', 'Open Products', 'Getting started with Manufacturing ERP Set up items, bills of material, work centers and users in your first week. manufacturing erp setup items bom onboarding',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='GETTING_STARTED' AND slug='getting-started-manufacturing-erp');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'PRODUCT_GUIDE', 'production-planning-guide', 'Production planning guide', 'The production flow
Production
Customer order
MRP run
Work order
Material issue
Production entry
Inspection
Finished goods
MRP compares demand with stock and open purchase orders, then proposes purchase and production orders. A planner firms the proposals into work orders, which release material to the shop floor.
Work order statuses
Status
Meaning
Planned
Proposed by MRP
Released
Material can be issued
In progress
Production has started
Completed
Finished goods received
Shortages
A work order cannot be released while a mandatory component has no stock and no open purchase order.
Watch: production management demo
Watch: the MRP process in eight minutes', 'From customer order to finished goods: demand, MRP, work orders and shop-floor reporting.', ('[{"type": "heading", "text": "The production flow", "level": 2}, {"type": "workflow_diagram", "title": "Production", "steps": [{"title": "Customer order"}, {"title": "MRP run"}, {"title": "Work order"}, {"title": "Material issue"}, {"title": "Production entry"}, {"title": "Inspection"}, {"title": "Finished goods"}]}, {"type": "paragraph", "text": "MRP compares demand with stock and open purchase orders, then proposes purchase and production orders. A planner firms the proposals into work orders, which release material to the shop floor."}, {"type": "table", "caption": "Work order statuses", "rows": [["Status", "Meaning"], ["Planned", "Proposed by MRP"], ["Released", "Material can be issued"], ["In progress", "Production has started"], ["Completed", "Finished goods received"]]}, {"type": "warning", "title": "Shortages", "text": "A work order cannot be released while a mandatory component has no stock and no open purchase order."}, {"type": "video", "contentId": ' || (SELECT id FROM knowledge_article WHERE content_type='VIDEO' AND slug='rootstock-production-management-demo') || ', "caption": "Watch: production management demo"}, {"type": "video", "contentId": ' || (SELECT id FROM knowledge_article WHERE content_type='VIDEO' AND slug='mrp-process-in-8-minutes') || ', "caption": "Watch: the MRP process in eight minutes"}]'), NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='production'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'production planning,mrp,work orders,shop floor', 'production planning mrp work orders shop floor capacity', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'INTERMEDIATE', NULL, NULL, 'Production planning guide From customer order to finished goods: demand, MRP, work orders and shop-floor reporting. production planning mrp work orders shop floor',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='PRODUCT_GUIDE' AND slug='production-planning-guide');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'STUDY_MATERIAL', 'bill-of-materials-and-routings', 'Bill of materials and routings explained', 'Bill of materials
Level 0: the finished item
Level 1: sub-assemblies and bought-out parts
Level 2 and below: components and raw materials
Worked example
A shaft needs 2.0 kg of steel bar at 80 per kg, with 5% scrap allowance, and 20 minutes on a lathe at 600 per hour. The material cost is 168 and the machining cost is 200, so the standard cost is 368.
material = qty * (1 + scrap) * rate
standard cost = material + sum(routing time * work center rate)
Routings
A routing lists the operations, the work center for each, and the time per unit. MRP and capacity planning both read it.
Watch: bill of materials explained', 'Multi-level BOMs, scrap allowance and routings, with a worked cost example.', ('[{"type": "heading", "text": "Bill of materials", "level": 2}, {"type": "bulleted_list", "items": ["Level 0: the finished item", "Level 1: sub-assemblies and bought-out parts", "Level 2 and below: components and raw materials"]}, {"type": "heading", "text": "Worked example", "level": 2}, {"type": "paragraph", "text": "A shaft needs 2.0 kg of steel bar at 80 per kg, with 5% scrap allowance, and 20 minutes on a lathe at 600 per hour. The material cost is 168 and the machining cost is 200, so the standard cost is 368."}, {"type": "code", "language": "text", "text": "material = qty * (1 + scrap) * rate\nstandard cost = material + sum(routing time * work center rate)"}, {"type": "callout", "title": "Routings", "text": "A routing lists the operations, the work center for each, and the time per unit. MRP and capacity planning both read it."}, {"type": "video", "contentId": ' || (SELECT id FROM knowledge_article WHERE content_type='VIDEO' AND slug='bom-explained-manufacturing-erp') || ', "caption": "Watch: bill of materials explained"}]'), NULL,
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='valam'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='valam' AND km.slug='bom'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'bom,routing,scrap,costing', 'bill of materials routing scrap allowance cost roll-up', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'INTERMEDIATE', NULL, NULL, 'Bill of materials and routings explained Multi-level BOMs, scrap allowance and routings, with a worked cost example. bom routing scrap costing',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='STUDY_MATERIAL' AND slug='bill-of-materials-and-routings');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'FAQ', 'how-do-i-issue-material-to-a-work-order', 'How do I issue material to a work order?', 'How do I issue material to a work order?
Release the work order and open Material Issue. The BOM components are listed with the quantity needed; confirm the batch or lot and issue them from stores. Stock reduces at once.', 'Release the work order, then issue the BOM components from stores.', '[{"type": "faq", "question": "How do I issue material to a work order?", "answer": "Release the work order and open Material Issue. The BOM components are listed with the quantity needed; confirm the batch or lot and issue them from stores. Stock reduces at once."}]', '{"question": "How do I issue material to a work order?", "answer": "Release the work order and open Material Issue. The BOM components are listed with the quantity needed; confirm the batch or lot and issue them from stores. Stock reduces at once."}',
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='production'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'work order,material issue,faq', 'issue material work order stores components', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'How do I issue material to a work order? Release the work order, then issue the BOM components from stores. work order material issue faq How do I issue material to a work order? Release the work order and open Material Issue. The BOM components are listed with the quantity needed; confirm the batch or lot and issue them from stores. Stock reduces at once.',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='FAQ' AND slug='how-do-i-issue-material-to-a-work-order');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'FAQ', 'can-incoming-material-be-rejected', 'Can I reject incoming material after inward entry?', 'Can I reject incoming material after inward entry?
Yes. Material waits in the inspection store until it is inspected. Rejected quantities move to the rejection store and can be returned to the supplier; only accepted quantities become usable stock.', 'Yes. Rejected quantities move to the rejection store and never reach usable stock.', '[{"type": "faq", "question": "Can I reject incoming material after inward entry?", "answer": "Yes. Material waits in the inspection store until it is inspected. Rejected quantities move to the rejection store and can be returned to the supplier; only accepted quantities become usable stock."}]', '{"question": "Can I reject incoming material after inward entry?", "answer": "Yes. Material waits in the inspection store until it is inspected. Rejected quantities move to the rejection store and can be returned to the supplier; only accepted quantities become usable stock."}',
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='quality'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'inspection,reject,inward,faq', 'reject incoming material inward inspection rejection store', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'Can I reject incoming material after inward entry? Yes. Rejected quantities move to the rejection store and never reach usable stock. inspection reject inward faq Can I reject incoming material after inward entry? Yes. Material waits in the inspection store until it is inspected. Rejected quantities move to the rejection store and can be returned to the supplier; only accepted quantities become usable stock.',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='FAQ' AND slug='can-incoming-material-be-rejected');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'TROUBLESHOOTING', 'work-order-has-material-shortage', 'Work order cannot be released: material shortage', 'The Release button is disabled, or releasing shows a shortage message.
Step 1
Open the shortage report on the work order and note the missing components.
Step 2
Check stock and open purchase orders for each. Raise a purchase order if none exists.
Step 3
Release the work order after the material is received, or allow a partial release if your policy permits.', 'A work order stays Planned because a component has no stock or purchase order.', '[{"type": "paragraph", "text": "The Release button is disabled, or releasing shows a shortage message."}, {"type": "step", "title": "Step 1", "text": "Open the shortage report on the work order and note the missing components."}, {"type": "step", "title": "Step 2", "text": "Check stock and open purchase orders for each. Raise a purchase order if none exists."}, {"type": "step", "title": "Step 3", "text": "Release the work order after the material is received, or allow a partial release if your policy permits."}]', '{"problem": "A work order cannot be released and shows a material shortage.", "cause": "A mandatory BOM component has no available stock and no open purchase order.", "solution": "Open the shortage report, raise or expedite the purchase order for each missing component, and release the work order once the material is received.", "code": "MFG-WO-409"}',
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='production'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'work order,shortage,troubleshooting', 'work order release material shortage component stock', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'INTERMEDIATE', NULL, NULL, 'Work order cannot be released: material shortage A work order stays Planned because a component has no stock or purchase order. work order shortage troubleshooting A work order cannot be released and shows a material shortage. A mandatory BOM component has no available stock and no open purchase order. Open the shortage report, raise or expedite the purchase order for each missing component, and release the work order once the material is received. MFG-WO-409',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='TROUBLESHOOTING' AND slug='work-order-has-material-shortage');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'ERROR_CODE', 'mfg-wo-409-material-shortage', 'MFG-WO-409: Material shortage on release', 'This error appears when a work order is released while one or more mandatory components are short.
MFG-WO-409 Material shortage: 2 components cannot be issued', 'The work order cannot be released because components are short.', '[{"type": "paragraph", "text": "This error appears when a work order is released while one or more mandatory components are short."}, {"type": "code", "language": "text", "text": "MFG-WO-409 Material shortage: 2 components cannot be issued"}]', '{"code": "MFG-WO-409", "error": "Material shortage: components cannot be issued", "cause": "Available stock is below the quantity the work order needs, and no receipt is due before the start date.", "solution": "Receive or transfer the missing stock, or reduce the work order quantity, then release again.", "permission": "RELEASE_WORK_ORDER"}',
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='thiran'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='thiran' AND km.slug='production'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'error code,work order,shortage', 'MFG-WO-409 work order material shortage release', 'PUBLIC', 'PUBLISHED', '1.0', NULL, '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'ADVANCED', NULL, NULL, 'MFG-WO-409: Material shortage on release The work order cannot be released because components are short. error code work order shortage MFG-WO-409 Material shortage: components cannot be issued Available stock is below the quantity the work order needs, and no receipt is due before the start date. Receive or transfer the missing stock, or reduce the work order quantity, then release again. RELEASE_WORK_ORDER',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='ERROR_CODE' AND slug='mfg-wo-409-material-shortage');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'RELEASE_NOTE', 'manufacturing-erp-release-1-1', 'Manufacturing ERP 1.1', 'What is new
Multi-level BOM explosion in one screen
Inspection plans for incoming material
Work order shortage report', 'Multi-level BOM explosion, inspection plans and a shortage report.', '[{"type": "heading", "text": "What is new", "level": 2}, {"type": "bulleted_list", "items": ["Multi-level BOM explosion in one screen", "Inspection plans for incoming material", "Work order shortage report"]}]', '{"version": "1.1", "releaseDate": "2026-09-30", "newFeatures": ["Multi-level BOM explosion in one screen", "Inspection plans for incoming material"], "improvements": ["MRP runs about twice as fast for large item masters"], "bugFixes": ["Scrap allowance is now applied once in cost roll-up"], "deprecated": ["The single-level BOM report is replaced by the multi-level report"], "notices": "Review inspection plans before the first incoming receipt."}',
  'PUBLISHED', 1, (SELECT id FROM knowledge_product WHERE slug='valam'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='valam' AND km.slug='bom'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'release,manufacturing erp,1.1', 'release notes manufacturing erp 1.1 bom inspection shortage', 'PUBLIC', 'PUBLISHED', '1.0', '1.1', '2026.4',
  now(), now() + interval '180 days',
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), (SELECT keycloak_sub FROM customer WHERE id=2), (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, NULL, NULL, NULL, 'Manufacturing ERP 1.1 Multi-level BOM explosion, inspection plans and a shortage report. release manufacturing erp 1.1 1.1 2026-09-30 Review inspection plans before the first incoming receipt.',
  now(), now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='RELEASE_NOTE' AND slug='manufacturing-erp-release-1-1');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'DOCUMENT', 'bom-import-checklist', 'BOM import checklist', 'Every component exists in the item master
One row per component with parent item, quantity and unit
Scrap allowance as a percentage
Effective dates for revisions', 'Prepare items, components and quantities before importing bills of material.', '[{"type": "bulleted_list", "items": ["Every component exists in the item master", "One row per component with parent item, quantity and unit", "Scrap allowance as a percentage", "Effective dates for revisions"]}]', '{"fileType": "PDF", "version": "1.0"}',
  'DRAFT', 1, (SELECT id FROM knowledge_product WHERE slug='valam'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='valam' AND km.slug='bom'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'import,checklist,bom', 'bom import checklist items components quantities', 'PUBLIC', 'IN_REVIEW', '0.1', NULL, NULL,
  NULL, NULL,
  (SELECT keycloak_sub FROM customer WHERE id=4), (SELECT keycloak_sub FROM customer WHERE id=3), NULL, (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'BOM import checklist Prepare items, components and quantities before importing bills of material. import checklist bom PDF 1.0',
  NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='DOCUMENT' AND slug='bom-import-checklist');

INSERT INTO knowledge_article (content_type, slug, title, body, short_description, blocks, type_fields, status, version, product_id, module_id, category_id,
  feature, tags, keywords, audience, workflow_state, current_version_label, product_version, documentation_version, effective_at, review_at,
  author_sub, reviewer_sub, approver_sub, updated_by_sub, featured, difficulty, direct_action_route, direct_action_label, search_text,
  published_at, created_at, updated_at)
SELECT 'COURSE', 'manufacturing-erp-fundamentals', 'Manufacturing ERP fundamentals', 'Four video lessons that take you from a customer enquiry to an inspected, invoiced order.', 'A short course: order to cash, BOM, MRP and quality, with video lessons.', '[{"type": "paragraph", "text": "Four video lessons that take you from a customer enquiry to an inspected, invoiced order."}]', NULL,
  'DRAFT', 1, (SELECT id FROM knowledge_product WHERE slug='valam'), (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug='valam' AND km.slug='bom'), (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  NULL, 'course,manufacturing erp,training', 'course manufacturing erp fundamentals training', 'PUBLIC', 'DRAFT', '0.1', NULL, NULL,
  NULL, NULL,
  (SELECT keycloak_sub FROM customer WHERE id=4), NULL, NULL, (SELECT keycloak_sub FROM customer WHERE id=4), FALSE, 'BEGINNER', NULL, NULL, 'Manufacturing ERP fundamentals A short course: order to cash, BOM, MRP and quality, with video lessons. course manufacturing erp training',
  NULL, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM knowledge_article WHERE content_type='COURSE' AND slug='manufacturing-erp-fundamentals');

-- Related content links
UPDATE knowledge_article SET related_content_ids=(SELECT ',' || string_agg(id::text, ',' ORDER BY id) || ',' FROM knowledge_article WHERE slug IN ('evyoog-introduction','bom-explained-manufacturing-erp')) WHERE content_type='GETTING_STARTED' AND slug='getting-started-manufacturing-erp' AND related_content_ids IS NULL;
UPDATE knowledge_article SET related_content_ids=(SELECT ',' || string_agg(id::text, ',' ORDER BY id) || ',' FROM knowledge_article WHERE slug IN ('mrp-process-in-8-minutes','work-order-has-material-shortage')) WHERE content_type='PRODUCT_GUIDE' AND slug='production-planning-guide' AND related_content_ids IS NULL;
UPDATE knowledge_article SET related_content_ids=(SELECT ',' || string_agg(id::text, ',' ORDER BY id) || ',' FROM knowledge_article WHERE slug IN ('bom-explained-manufacturing-erp','production-planning-guide')) WHERE content_type='STUDY_MATERIAL' AND slug='bill-of-materials-and-routings' AND related_content_ids IS NULL;

-- 3. YouTube video rows (thumbnail = YouTube hqdefault)
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=Nam6A2-KHxo', 'Nam6A2-KHxo', 'https://i.ytimg.com/vi/Nam6A2-KHxo/hqdefault.jpg', 87, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-introduction'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=Fu7MOCnk9GE', 'Fu7MOCnk9GE', 'https://i.ytimg.com/vi/Fu7MOCnk9GE/hqdefault.jpg', 334, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-enquiry'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=k2FPrJDMpfs', 'k2FPrJDMpfs', 'https://i.ytimg.com/vi/k2FPrJDMpfs/hqdefault.jpg', 227, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-lead'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=iZ7-1RFVmho', 'iZ7-1RFVmho', 'https://i.ytimg.com/vi/iZ7-1RFVmho/hqdefault.jpg', 204, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-quotation'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=VDCWuKvmnaA', 'VDCWuKvmnaA', 'https://i.ytimg.com/vi/VDCWuKvmnaA/hqdefault.jpg', 246, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-order-booking'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=XhVy_vGN5LY', 'XhVy_vGN5LY', 'https://i.ytimg.com/vi/XhVy_vGN5LY/hqdefault.jpg', 272, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-inward'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=naaNpAQhK78', 'naaNpAQhK78', 'https://i.ytimg.com/vi/naaNpAQhK78/hqdefault.jpg', 291, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-inward-inspection'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=d_cVha-JhUU', 'd_cVha-JhUU', 'https://i.ytimg.com/vi/d_cVha-JhUU/hqdefault.jpg', 290, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-invoice'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=ElJO_NxpXGE', 'ElJO_NxpXGE', 'https://i.ytimg.com/vi/ElJO_NxpXGE/hqdefault.jpg', 350, 'Nagaraj Samiyappan'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='evyoog-payroll'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=Nqf_k7INTdw', 'Nqf_k7INTdw', 'https://i.ytimg.com/vi/Nqf_k7INTdw/hqdefault.jpg', 285, 'ERP Software Experts'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='bom-explained-manufacturing-erp'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=u3P6YMI5Ah0', 'u3P6YMI5Ah0', 'https://i.ytimg.com/vi/u3P6YMI5Ah0/hqdefault.jpg', 482, 'Educationleaves'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='mrp-process-in-8-minutes'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=n03mdbrNQYM', 'n03mdbrNQYM', 'https://i.ytimg.com/vi/n03mdbrNQYM/hqdefault.jpg', 328, 'Lighthouse Alchemy ERP Academy'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='incoming-quality-control-manufacturing-erp'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=ZqRToQInbC8', 'ZqRToQInbC8', 'https://i.ytimg.com/vi/ZqRToQInbC8/hqdefault.jpg', 204, 'Rootstock Software'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='rootstock-production-management-demo'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);
INSERT INTO knowledge_video (content_id, video_source_type, video_url, video_id, thumbnail_url, duration_seconds, channel)
SELECT a.id, 'YOUTUBE', 'https://www.youtube.com/watch?v=wtKqCdf_g5I', 'wtKqCdf_g5I', 'https://i.ytimg.com/vi/wtKqCdf_g5I/hqdefault.jpg', 1497, 'Cybrosys Technologies'
FROM knowledge_article a WHERE a.content_type='VIDEO' AND a.slug='odoo-16-manufacturing-module-demo'
  AND NOT EXISTS (SELECT 1 FROM knowledge_video x WHERE x.content_id=a.id);

-- 4. Published version snapshots, then point each published item at its live version
INSERT INTO knowledge_content_version (content_id, version_label, title, short_description, blocks, type_fields, body, search_text, audience,
  require_product_access, effective_at, published_by_sub, published_at)
SELECT a.id, '1.0', a.title, a.short_description, a.blocks, a.type_fields, a.body, a.search_text, a.audience, FALSE, a.effective_at, a.approver_sub, now()
FROM knowledge_article a
WHERE a.workflow_state='PUBLISHED' AND a.live_version_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM knowledge_content_version v WHERE v.content_id=a.id AND v.version_label='1.0');
UPDATE knowledge_article a SET live_version_id=v.id, current_version_label='1.0'
FROM knowledge_content_version v
WHERE v.content_id=a.id AND v.version_label='1.0' AND a.workflow_state='PUBLISHED' AND a.live_version_id IS NULL;

-- 5. Fill the empty taxonomy and metadata fields of the pre-existing items (glossary, workflow guides, templates, welcome article)
UPDATE knowledge_article a SET
  product_id = COALESCE(a.product_id, (SELECT id FROM knowledge_product WHERE slug=m.pslug)),
  module_id = COALESCE(a.module_id, (SELECT km.id FROM knowledge_module km JOIN knowledge_product kp ON kp.id=km.product_id WHERE kp.slug=m.pslug AND km.slug=m.mslug)),
  category_id = COALESCE(a.category_id, (SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL)),
  tags = COALESCE(a.tags, lower(a.title) || ',' || lower(a.content_type)),
  keywords = COALESCE(a.keywords, lower(a.title)),
  difficulty = COALESCE(a.difficulty, 'BEGINNER'),
  author_sub = COALESCE(a.author_sub, (SELECT keycloak_sub FROM customer WHERE id=4)),
  updated_by_sub = COALESCE(a.updated_by_sub, (SELECT keycloak_sub FROM customer WHERE id=4))
FROM (VALUES
 ('BOM','valam','bom'),('MRP','thittam','mrp'),('OEE','thiran','oee'),('WIP','thiran','production'),('S&OP','thittam','s-and-op'),
 ('MES','thiran','mes'),('QMS','thiran','qms'),('CAPA','thiran','quality'),('RFQ','varthan','procurement'),('MOQ','varthan','purchase'),
 ('EOQ','varthan','stock'),('Procure-to-pay','varthan','procurement'),('Lead-to-cash','varthan','sales'),('Production','thiran','production'),
 ('Purchase order template','varthan','purchase'),('RFQ template','varthan','procurement'),('Employee import template','valam','hr'),
 ('Attendance import template','valam','attendance'),('Chart of accounts template','valam','finance'),('Opening balance template','valam','finance'),
 ('BOM template','valam','bom'),('Routing template','thiran','production')) AS m(title,pslug,mslug)
WHERE a.title=m.title AND a.product_id IS NULL;
UPDATE knowledge_article SET category_id=(SELECT id FROM knowledge_category WHERE slug='general' AND scope IS NULL),
  tags=COALESCE(tags,'getting started,account,sign in'), keywords=COALESCE(keywords,'account sign in login getting started'),
  difficulty=COALESCE(difficulty,'BEGINNER'), featured=TRUE, author_sub=COALESCE(author_sub,(SELECT keycloak_sub FROM customer WHERE id=4))
WHERE title='Getting started with your eVyoog account' AND category_id IS NULL;
UPDATE knowledge_article SET short_description = COALESCE(short_description, left(body,200)) WHERE short_description IS NULL;


-- 6. Course with video lessons (Academy tables, prepared only - the course itself stays a draft)
INSERT INTO knowledge_course (content_id, certificate_name, pass_percent)
SELECT a.id, 'Manufacturing ERP Fundamentals', 70 FROM knowledge_article a WHERE a.content_type='COURSE' AND a.slug='manufacturing-erp-fundamentals'
  AND NOT EXISTS (SELECT 1 FROM knowledge_course c WHERE c.content_id=a.id);
INSERT INTO knowledge_lesson (course_id, lesson_order, content_id, title)
SELECT c.id, l.ord, (SELECT id FROM knowledge_article WHERE content_type='VIDEO' AND slug=l.slug), l.title
FROM knowledge_course c JOIN knowledge_article ca ON ca.id=c.content_id AND ca.slug='manufacturing-erp-fundamentals',
 (VALUES (1,'evyoog-enquiry','From enquiry to order'),(2,'bom-explained-manufacturing-erp','Understand the bill of materials'),(3,'mrp-process-in-8-minutes','Plan with MRP'),(4,'incoming-quality-control-manufacturing-erp','Inspect incoming material')) AS l(ord,slug,title)
WHERE NOT EXISTS (SELECT 1 FROM knowledge_lesson x WHERE x.course_id=c.id AND x.lesson_order=l.ord);


-- 7. Search index entries so Knowledge Center search finds the new public items (the backend re-indexes on change)
INSERT INTO search_document (source_type, source_id, visibility, reference, title, body, keywords, title_folded, body_folded, keywords_folded, content_updated_at, indexed_at)
SELECT 'KNOWLEDGE', a.id, 'PUBLIC', '#'||a.id, a.title, a.body||E'\n'||COALESCE(a.search_text,''),
  lower(replace(a.content_type,'_',' '))||COALESCE(' '||replace(a.tags,',',' '),'')||COALESCE(' '||a.keywords,''),
  trim(regexp_replace(lower(a.title),'[^[:alnum:]]+',' ','g')),
  trim(regexp_replace(lower(a.body||' '||COALESCE(a.search_text,'')),'[^[:alnum:]]+',' ','g')),
  trim(regexp_replace(lower(lower(replace(a.content_type,'_',' '))||COALESCE(' '||replace(a.tags,',',' '),'')||COALESCE(' '||a.keywords,'')),'[^[:alnum:]]+',' ','g')),
  a.published_at, now()
FROM knowledge_article a
WHERE a.workflow_state='PUBLISHED' AND a.audience='PUBLIC' AND NOT a.require_product_access
  AND NOT EXISTS (SELECT 1 FROM search_document d WHERE d.source_type='KNOWLEDGE' AND d.source_id=a.id);


-- 8. Reader activity: feedback, bookmarks, progress and analytics events for the demo customers (ids 2, 3, 4)
INSERT INTO knowledge_feedback (content_id, version_label, customer_id, voter_sub, kind, helpful, reason, comment_text, created_at)
SELECT a.id, '1.0', c.id, c.keycloak_sub, f.kind, f.helpful, f.reason, f.comment, now() - f.ago * interval '1 day'
FROM (VALUES
 ('evyoog-introduction',2,'VOTE',TRUE,NULL,NULL,6),('evyoog-introduction',3,'VOTE',TRUE,NULL,'Good quick overview.',5),
 ('evyoog-order-booking',4,'VOTE',TRUE,NULL,NULL,4),('evyoog-quotation',2,'VOTE',TRUE,NULL,'Easy to follow.',3),
 ('mrp-process-in-8-minutes',3,'VOTE',FALSE,'NOT_OUR_ERP','Generic, not specific to eVyoog.',3),
 ('bill-of-materials-and-routings',2,'VOTE',TRUE,NULL,NULL,2),
 ('work-order-has-material-shortage',3,'OUTDATED',NULL,'SCREENSHOT_OLD','The shortage report screen has changed.',2),
 ('production-planning-guide',4,'SUGGESTION',NULL,NULL,'Please add a section on capacity planning.',1)
) AS f(slug,cust,kind,helpful,reason,comment,ago)
JOIN knowledge_article a ON a.slug=f.slug JOIN customer c ON c.id=f.cust
WHERE NOT EXISTS (SELECT 1 FROM knowledge_feedback x WHERE x.content_id=a.id AND x.customer_id=c.id AND x.kind=f.kind);

INSERT INTO knowledge_bookmark (customer_id, content_id)
SELECT f.cust, a.id FROM (VALUES (2,'evyoog-introduction'),(2,'bill-of-materials-and-routings'),(3,'evyoog-enquiry'),(4,'getting-started-manufacturing-erp'),(4,'production-planning-guide')) AS f(cust,slug)
JOIN knowledge_article a ON a.slug=f.slug ON CONFLICT (customer_id, content_id) DO NOTHING;

INSERT INTO knowledge_progress (customer_id, content_id, percent, position_seconds, completed_at, last_viewed_at)
SELECT f.cust, a.id, f.pct, f.pos, CASE WHEN f.pct=100 THEN now() - f.ago * interval '1 day' END, now() - f.ago * interval '1 day'
FROM (VALUES (2,'evyoog-introduction',100,87,5),(2,'evyoog-enquiry',45,150,3),(3,'mrp-process-in-8-minutes',20,96,2),
             (3,'evyoog-lead',100,227,3),(4,'odoo-16-manufacturing-module-demo',60,898,1),(4,'getting-started-manufacturing-erp',100,NULL,4)) AS f(cust,slug,pct,pos,ago)
JOIN knowledge_article a ON a.slug=f.slug ON CONFLICT (customer_id, content_id) DO NOTHING;

INSERT INTO knowledge_event (event_type, content_id, version_label, content_type, product_id, module_id, source_type, organization_id, percent, seconds, viewer_hash, created_at)
SELECT e.ev, a.id, '1.0', a.content_type, a.product_id, a.module_id, CASE WHEN a.content_type='VIDEO' THEN 'YOUTUBE' END, NULL, e.pct, e.secs,
       md5('demo-viewer-'||e.cust), now() - e.ago * interval '1 day'
FROM (VALUES
 ('CONTENT_VIEWED','evyoog-introduction',2,NULL,NULL,6),('VIDEO_PLAYED','evyoog-introduction',2,NULL,NULL,6),('VIDEO_PROGRESS','evyoog-introduction',2,100,87,6),
 ('CONTENT_VIEWED','evyoog-enquiry',3,NULL,NULL,3),('VIDEO_PLAYED','evyoog-enquiry',3,NULL,NULL,3),('VIDEO_PROGRESS','evyoog-enquiry',3,45,150,3),
 ('CONTENT_VIEWED','mrp-process-in-8-minutes',3,NULL,NULL,2),('VIDEO_PLAYED','mrp-process-in-8-minutes',3,NULL,NULL,2),
 ('CONTENT_VIEWED','odoo-16-manufacturing-module-demo',4,NULL,NULL,1),('VIDEO_PLAYED','odoo-16-manufacturing-module-demo',4,NULL,NULL,1),('VIDEO_PROGRESS','odoo-16-manufacturing-module-demo',4,60,898,1),
 ('CONTENT_VIEWED','getting-started-manufacturing-erp',4,NULL,NULL,4),('CONTENT_VIEWED','bill-of-materials-and-routings',2,NULL,NULL,2),
 ('BOOKMARKED','evyoog-introduction',2,NULL,NULL,6),('FEEDBACK_GIVEN','evyoog-order-booking',4,NULL,NULL,4),('FEEDBACK_GIVEN','work-order-has-material-shortage',3,NULL,NULL,2),
 ('CONTENT_VIEWED','work-order-has-material-shortage',3,NULL,NULL,2),('CONTENT_VIEWED','production-planning-guide',4,NULL,NULL,1)
) AS e(ev,slug,cust,pct,secs,ago)
JOIN knowledge_article a ON a.slug=e.slug
WHERE NOT EXISTS (SELECT 1 FROM knowledge_event WHERE content_id=a.id);

COMMIT;
