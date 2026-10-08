package com.vyoog.eisplatform.modules.orghierarchy.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos.*;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgLevel;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNodeHistory;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgLevelRepository;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeHistoryRepository;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * REQ-TEN-006 Organization hierarchy. Every public method first resolves the
 * caller's organization through {@code MANAGE_ORGANIZATION} (BR-ORG-001/002),
 * then only touches rows of that organization.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrgHierarchyService {

    static final List<String> DEFAULT_LEVELS = List.of(
        "ORGANIZATION", "DIVISION", "BUSINESS_UNIT", "DEPARTMENT", "LOCATION", "COST_CENTER", "TEAM");
    static final String ROOT_TYPE = "ORGANIZATION";
    static final int MAX_IMPORT_ROWS = 1000;
    static final int MAX_IMPORT_BYTES = 1_000_000;
    static final int MAX_LEVELS = 20;
    private static final Pattern TYPE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{0,49}$");

    private final OrgNodeRepository nodeRepository;
    private final OrgLevelRepository levelRepository;
    private final OrgNodeHistoryRepository historyRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final OrganizationSelfService organizationSelfService;
    private final AuditService auditService;

    // ------------------------------------------------------------------ read

    public TreeDto tree(Long customerId) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        ensureInitialised(orgId);
        return buildTree(orgId);
    }

    public NodeDetailDto detail(Long customerId, Long nodeId) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        ensureInitialised(orgId);
        return detailOf(orgId, nodeId);
    }

    public List<HistoryDto> history(Long customerId, Long nodeId) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        return historyOf(orgId, nodeId);
    }

    /** REQ-TEN-007: read-only views for the platform administrator (BR-DIR-008). The caller's
     * permission is checked by the security configuration; these never create the root. */
    public TreeDto adminTree(Long orgId) {
        if (!nodeRepository.existsByOrganizationIdAndParentIdIsNull(orgId)) {
            List<LevelDto> levels = new ArrayList<>();
            for (int i = 0; i < DEFAULT_LEVELS.size(); i++) {
                levels.add(new LevelDto(DEFAULT_LEVELS.get(i), prettify(DEFAULT_LEVELS.get(i)), i));
            }
            return new TreeDto(levels, List.of());
        }
        return buildTree(orgId);
    }

    public NodeDetailDto adminDetail(Long orgId, Long nodeId) {
        return detailOf(orgId, nodeId);
    }

    public List<HistoryDto> adminHistory(Long orgId, Long nodeId) {
        return historyOf(orgId, nodeId);
    }

    /** Number of hierarchy nodes of an organization (0 when never opened). */
    public int nodeCount(Long orgId) {
        return nodeRepository.findByOrganizationIdOrderBySortOrderAscNameAsc(orgId).size();
    }

    private NodeDetailDto detailOf(Long orgId, Long nodeId) {
        OrgNode node = requireNode(orgId, nodeId);
        Map<Long, OrgNode> byId = nodesById(orgId);
        LinkedList<String> path = new LinkedList<>();
        for (OrgNode n = node; n != null; n = n.getParentId() == null ? null : byId.get(n.getParentId())) {
            path.addFirst(n.getName());
        }
        List<NodeMemberDto> members = memberRepository.findByOrgNodeId(nodeId).stream()
            .filter(m -> m.getOrganizationId().equals(orgId))
            .map(this::toMemberDto)
            .toList();
        return new NodeDetailDto(toDto(node, byId.values(), memberCounts(orgId)), path, members);
    }

    private List<HistoryDto> historyOf(Long orgId, Long nodeId) {
        requireNode(orgId, nodeId);
        Map<Long, OrgNode> byId = nodesById(orgId);
        return historyRepository.findByOrgNodeIdOrderByEffectiveAtDescIdDesc(nodeId).stream()
            .map(h -> new HistoryDto(h.getId(), h.getPreviousParentId(), nameOf(byId, h.getPreviousParentId()),
                h.getNewParentId(), nameOf(byId, h.getNewParentId()), h.getChangedByCustomerId(), h.getEffectiveAt()))
            .toList();
    }

    public List<LevelDto> levels(Long customerId) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        ensureInitialised(orgId);
        return levelDtos(orgId);
    }

    // ----------------------------------------------------------------- write

    public NodeDto create(Long customerId, CreateNodeRequest request) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        ensureInitialised(orgId);
        if (request.parentId() == null) {
            throw new IllegalArgumentException("A node must be created under a parent; the organization already has its root");
        }
        OrgNode node = createNode(orgId, request.parentId(), request.name(), request.type(), request.code(),
            request.description(), request.sortOrder(), levelRanks(orgId), nodesById(orgId));
        audit("ORG_NODE_CREATED", customerId, orgId, node, "Created node " + node.getName() + " (" + node.getNodeType() + ")");
        return dto(orgId, node);
    }

    public NodeDto update(Long customerId, Long nodeId, UpdateNodeRequest request) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        ensureInitialised(orgId);
        OrgNode node = requireNode(orgId, nodeId);
        Map<Long, OrgNode> byId = nodesById(orgId);
        Map<String, Integer> ranks = levelRanks(orgId);
        boolean isRoot = node.getParentId() == null;

        String name = cleanName(request.name());
        String type = request.type() == null ? node.getNodeType() : normaliseType(request.type());
        if (isRoot && !type.equals(node.getNodeType())) {
            throw new IllegalArgumentException("The root of the organization keeps its type");
        }
        if (!name.equalsIgnoreCase(node.getName()) && !isRoot) {
            assertNameFree(byId.values(), node.getParentId(), name, node.getId());
        }
        if (!type.equals(node.getNodeType())) {
            int rank = rankOf(ranks, type);
            OrgNode parent = byId.get(node.getParentId());
            if (parent != null && rank <= rankOf(ranks, parent.getNodeType())) {
                throw new IllegalArgumentException("A " + label(type) + " cannot sit under a " + label(parent.getNodeType()));
            }
            for (OrgNode child : childrenOf(byId.values(), nodeId)) {
                if (rankOf(ranks, child.getNodeType()) <= rank) {
                    throw new IllegalArgumentException("Its child " + child.getName() + " (" + label(child.getNodeType())
                        + ") would no longer be below it");
                }
            }
        }
        boolean wasActive = node.isActive();
        if (request.active() != null && request.active() != wasActive) {
            if (!request.active()) {
                if (isRoot) {
                    throw new IllegalArgumentException("The root of the organization cannot be deactivated");
                }
                int children = childrenOf(byId.values(), nodeId).size();
                long members = memberRepository.countByOrgNodeId(nodeId);
                if ((children > 0 || members > 0) && !Boolean.TRUE.equals(request.force())) {
                    throw new InvalidStateException("This node has " + children + " child node(s) and " + members
                        + " member(s). Confirm to deactivate it anyway");
                }
            }
            node.setActive(request.active());
        }
        node.setName(name);
        node.setNodeType(type);
        node.setCode(blankToNull(request.code(), 50, "Code"));
        node.setDescription(blankToNull(request.description(), 1000, "Description"));
        if (request.sortOrder() != null) {
            node.setSortOrder(request.sortOrder());
        }
        node.setUpdatedAt(Instant.now());
        node = nodeRepository.save(node);
        String action = request.active() != null && request.active() != wasActive
            ? (request.active() ? "ORG_NODE_ACTIVATED" : "ORG_NODE_DEACTIVATED") : "ORG_NODE_UPDATED";
        audit(action, customerId, orgId, node, "Updated node " + node.getName());
        return dto(orgId, node);
    }

    public NodeDto move(Long customerId, Long nodeId, Long newParentId) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        ensureInitialised(orgId);
        OrgNode node = requireNode(orgId, nodeId);
        Map<Long, OrgNode> byId = nodesById(orgId);
        if (node.getParentId() == null) {
            throw new IllegalArgumentException("The root of the organization cannot be moved");
        }
        if (newParentId == null) {
            throw new IllegalArgumentException("Choose the node to move it under");
        }
        OrgNode parent = byId.get(newParentId);
        if (parent == null) {
            throw new ResourceNotFoundException("Node not found");
        }
        if (newParentId.equals(node.getParentId())) {
            throw new IllegalArgumentException("The node is already under that parent");
        }
        for (OrgNode n = parent; n != null; n = n.getParentId() == null ? null : byId.get(n.getParentId())) {
            if (n.getId().equals(nodeId)) {
                throw new IllegalArgumentException("A node cannot be moved under itself or one of its descendants");
            }
        }
        Map<String, Integer> ranks = levelRanks(orgId);
        if (rankOf(ranks, node.getNodeType()) <= rankOf(ranks, parent.getNodeType())) {
            throw new IllegalArgumentException("A " + label(node.getNodeType()) + " cannot sit under a " + label(parent.getNodeType()));
        }
        assertNameFree(byId.values(), newParentId, node.getName(), nodeId);

        OrgNodeHistory history = new OrgNodeHistory();
        history.setOrganizationId(orgId);
        history.setOrgNodeId(nodeId);
        history.setPreviousParentId(node.getParentId());
        history.setNewParentId(newParentId);
        history.setChangedByCustomerId(customerId);
        historyRepository.save(history);

        Long oldParent = node.getParentId();
        node.setParentId(newParentId);
        node.setUpdatedAt(Instant.now());
        node = nodeRepository.save(node);
        audit("ORG_NODE_MOVED", customerId, orgId, node,
            "Moved node " + node.getName() + " from " + nameOf(byId, oldParent) + " to " + parent.getName());
        return dto(orgId, node);
    }

    public void delete(Long customerId, Long nodeId) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        OrgNode node = requireNode(orgId, nodeId);
        if (node.getParentId() == null) {
            throw new IllegalArgumentException("The root of the organization cannot be deleted");
        }
        int children = childrenOf(nodesById(orgId).values(), nodeId).size();
        if (children > 0) {
            throw new InvalidStateException("This node still has " + children + " child node(s). Move or delete them first");
        }
        long members = memberRepository.countByOrgNodeId(nodeId);
        if (members > 0) {
            throw new InvalidStateException("This node still has " + members + " member(s). Remove their placement first");
        }
        historyRepository.deleteByOrgNodeId(nodeId);
        nodeRepository.delete(node);
        audit("ORG_NODE_DELETED", customerId, orgId, node, "Deleted node " + node.getName());
    }

    public NodeDetailDto placeMember(Long customerId, Long nodeId, Long memberId) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        OrgNode node = requireNode(orgId, nodeId);
        OrganizationMember member = requireMember(orgId, memberId);
        member.setOrgNodeId(nodeId);
        memberRepository.save(member);
        audit("ORG_NODE_MEMBER_PLACED", customerId, orgId, node, "Placed member " + memberId + " on " + node.getName());
        return detail(customerId, nodeId);
    }

    public NodeDetailDto removeMember(Long customerId, Long nodeId, Long memberId) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        OrgNode node = requireNode(orgId, nodeId);
        OrganizationMember member = requireMember(orgId, memberId);
        if (nodeId.equals(member.getOrgNodeId())) {
            member.setOrgNodeId(null);
            memberRepository.save(member);
            audit("ORG_NODE_MEMBER_REMOVED", customerId, orgId, node, "Removed member " + memberId + " from " + node.getName());
        }
        return detail(customerId, nodeId);
    }

    public List<LevelDto> updateLevels(Long customerId, UpdateLevelsRequest request) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        ensureInitialised(orgId);
        List<LevelInput> input = request == null || request.levels() == null ? List.of() : request.levels();
        if (input.isEmpty() || input.size() > MAX_LEVELS) {
            throw new IllegalArgumentException("Provide between 1 and " + MAX_LEVELS + " levels");
        }
        LinkedHashMap<String, String> wanted = new LinkedHashMap<>();
        for (LevelInput level : input) {
            String type = normaliseType(level.type());
            if (wanted.containsKey(type)) {
                throw new IllegalArgumentException("Level " + type + " appears twice");
            }
            String label = level.label() == null || level.label().isBlank() ? prettify(type) : level.label().trim();
            if (label.length() > 100) {
                throw new IllegalArgumentException("A level label is limited to 100 characters");
            }
            wanted.put(type, label);
        }
        if (!wanted.keySet().iterator().next().equals(ROOT_TYPE)) {
            throw new IllegalArgumentException("The first level must stay Organization");
        }
        Map<String, Integer> newRanks = new HashMap<>();
        int rank = 0;
        for (String type : wanted.keySet()) {
            newRanks.put(type, rank++);
        }
        Map<Long, OrgNode> byId = nodesById(orgId);
        for (OrgNode n : byId.values()) {
            if (!newRanks.containsKey(n.getNodeType())) {
                throw new InvalidStateException("Level " + label(n.getNodeType()) + " is used by node " + n.getName()
                    + " and cannot be removed");
            }
            OrgNode parent = n.getParentId() == null ? null : byId.get(n.getParentId());
            if (parent != null && newRanks.get(n.getNodeType()) <= newRanks.get(parent.getNodeType())) {
                throw new InvalidStateException("That order would put " + n.getName() + " (" + label(n.getNodeType())
                    + ") at or above its parent " + parent.getName());
            }
        }
        levelRepository.deleteAllInBatch(levelRepository.findByOrganizationIdOrderByLevelRank(orgId));
        levelRepository.flush();
        rank = 0;
        for (Map.Entry<String, String> e : wanted.entrySet()) {
            levelRepository.save(newLevel(orgId, e.getKey(), e.getValue(), rank++));
        }
        auditService.recordSuccess("ORG_LEVELS_CHANGED", null, customerId, null, "OrgLevel", orgId.toString(), orgId,
            "Level order set to " + String.join(" > ", wanted.keySet()));
        return levelDtos(orgId);
    }

    public ImportResultDto importCsv(Long customerId, byte[] content) {
        Long orgId = organizationSelfService.requireOrganizationManagement(customerId);
        ensureInitialised(orgId);
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("The file is empty");
        }
        if (content.length > MAX_IMPORT_BYTES) {
            throw new IllegalArgumentException("The file is larger than 1 MB");
        }
        List<List<String>> rows = parseCsv(new String(content, java.nio.charset.StandardCharsets.UTF_8));
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("The file is empty");
        }
        List<String> header = rows.get(0).stream().map(h -> h.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "")).toList();
        int iName = header.indexOf("name");
        int iType = header.indexOf("type");
        if (iName < 0 || iType < 0) {
            throw new IllegalArgumentException("The first row must contain the columns name and type");
        }
        int iCode = header.indexOf("code");
        int iDesc = header.indexOf("description");
        int iParent = header.indexOf("parentname");
        if (rows.size() - 1 > MAX_IMPORT_ROWS) {
            throw new IllegalArgumentException("The file has more than " + MAX_IMPORT_ROWS + " rows");
        }
        Map<String, Integer> ranks = levelRanks(orgId);
        Map<Long, OrgNode> byId = nodesById(orgId);
        OrgNode root = byId.values().stream().filter(n -> n.getParentId() == null).findFirst().orElseThrow();
        int created = 0;
        List<ImportError> errors = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            int rowNumber = i + 1;
            try {
                String parentName = cell(row, iParent);
                Long parentId = root.getId();
                if (!parentName.isBlank()) {
                    List<OrgNode> matches = byId.values().stream()
                        .filter(n -> n.getName().equalsIgnoreCase(parentName.trim())).toList();
                    if (matches.isEmpty()) {
                        throw new IllegalArgumentException("Parent not found: " + parentName);
                    }
                    if (matches.size() > 1) {
                        throw new IllegalArgumentException("Parent name is ambiguous: " + parentName);
                    }
                    parentId = matches.get(0).getId();
                }
                createNode(orgId, parentId, cell(row, iName), cell(row, iType), cell(row, iCode), cell(row, iDesc), null, ranks, byId);
                created++;
            } catch (IllegalArgumentException | DuplicateResourceException | ResourceNotFoundException | InvalidStateException e) {
                errors.add(new ImportError(rowNumber, e.getMessage()));
            }
        }
        auditService.recordSuccess("ORG_NODE_IMPORTED", null, customerId, null, "OrgNode", null, orgId,
            "CSV import: " + created + " created, " + errors.size() + " failed");
        return new ImportResultDto(created, errors.size(), errors);
    }

    // --------------------------------------------------------------- helpers

    /** Creates and saves a node (no audit); adds it to {@code byId}. */
    private OrgNode createNode(Long orgId, Long parentId, String rawName, String rawType, String code, String description,
                               Integer sortOrder, Map<String, Integer> ranks, Map<Long, OrgNode> byId) {
        OrgNode parent = byId.get(parentId);
        if (parent == null) {
            throw new ResourceNotFoundException("Node not found");
        }
        String name = cleanName(rawName);
        String type = normaliseType(rawType);
        if (rankOf(ranks, type) <= rankOf(ranks, parent.getNodeType())) {
            throw new IllegalArgumentException("A " + label(type) + " cannot sit under a " + label(parent.getNodeType()));
        }
        assertNameFree(byId.values(), parentId, name, null);
        OrgNode node = new OrgNode();
        node.setOrganizationId(orgId);
        node.setParentId(parentId);
        node.setName(name);
        node.setNodeType(type);
        node.setCode(blankToNull(code, 50, "Code"));
        node.setDescription(blankToNull(description, 1000, "Description"));
        node.setSortOrder(sortOrder == null ? 0 : sortOrder);
        node = nodeRepository.save(node);
        byId.put(node.getId(), node);
        return node;
    }

    private void ensureInitialised(Long orgId) {
        if (nodeRepository.existsByOrganizationIdAndParentIdIsNull(orgId)) {
            return;
        }
        Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        OrgNode root = new OrgNode();
        root.setOrganizationId(orgId);
        root.setName(cleanName(organization.getName().length() > 150 ? organization.getName().substring(0, 150) : organization.getName()));
        root.setNodeType(ROOT_TYPE);
        nodeRepository.save(root);
        if (levelRepository.findByOrganizationIdOrderByLevelRank(orgId).isEmpty()) {
            int rank = 0;
            for (String type : DEFAULT_LEVELS) {
                levelRepository.save(newLevel(orgId, type, prettify(type), rank++));
            }
        }
    }

    private OrgLevel newLevel(Long orgId, String type, String label, int rank) {
        OrgLevel level = new OrgLevel();
        level.setOrganizationId(orgId);
        level.setNodeType(type);
        level.setLabel(label);
        level.setLevelRank(rank);
        return level;
    }

    private TreeDto buildTree(Long orgId) {
        Map<Long, OrgNode> byId = nodesById(orgId);
        Map<Long, Integer> memberCounts = memberCounts(orgId);
        List<NodeDto> nodes = nodeRepository.findByOrganizationIdOrderBySortOrderAscNameAsc(orgId).stream()
            .map(n -> toDto(n, byId.values(), memberCounts)).toList();
        return new TreeDto(levelDtos(orgId), nodes);
    }

    private NodeDto dto(Long orgId, OrgNode node) {
        return toDto(node, nodesById(orgId).values(), memberCounts(orgId));
    }

    private NodeDto toDto(OrgNode n, Collection<OrgNode> all, Map<Long, Integer> memberCounts) {
        int children = childrenOf(all, n.getId()).size();
        return new NodeDto(n.getId(), n.getParentId(), n.getName(), n.getNodeType(), n.getCode(), n.getDescription(),
            n.getSortOrder(), n.isActive(), children, memberCounts.getOrDefault(n.getId(), 0),
            n.getCreatedAt(), n.getUpdatedAt());
    }

    private Map<Long, Integer> memberCounts(Long orgId) {
        Map<Long, Integer> counts = new HashMap<>();
        for (OrganizationMember m : memberRepository.findByOrganizationId(orgId)) {
            if (m.getOrgNodeId() != null) {
                counts.merge(m.getOrgNodeId(), 1, Integer::sum);
            }
        }
        return counts;
    }

    private Map<Long, OrgNode> nodesById(Long orgId) {
        return nodeRepository.findByOrganizationIdOrderBySortOrderAscNameAsc(orgId).stream()
            .collect(Collectors.toMap(OrgNode::getId, n -> n, (a, b) -> a, HashMap::new));
    }

    private static List<OrgNode> childrenOf(Collection<OrgNode> all, Long parentId) {
        return all.stream().filter(n -> parentId.equals(n.getParentId())).toList();
    }

    private OrgNode requireNode(Long orgId, Long nodeId) {
        return nodeRepository.findByIdAndOrganizationId(nodeId, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Node not found"));
    }

    private OrganizationMember requireMember(Long orgId, Long memberId) {
        return memberRepository.findById(memberId)
            .filter(m -> m.getOrganizationId().equals(orgId))
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    private NodeMemberDto toMemberDto(OrganizationMember m) {
        Customer c = customerRepository.findById(m.getCustomerId()).orElse(null);
        return new NodeMemberDto(m.getId(), m.getCustomerId(), c == null ? null : c.getFirstName(),
            c == null ? null : c.getLastName(), c == null ? null : c.getEmail(), m.getStatus().name());
    }

    private List<LevelDto> levelDtos(Long orgId) {
        return levelRepository.findByOrganizationIdOrderByLevelRank(orgId).stream()
            .map(l -> new LevelDto(l.getNodeType(), l.getLabel(), l.getLevelRank())).toList();
    }

    private Map<String, Integer> levelRanks(Long orgId) {
        Map<String, Integer> ranks = new HashMap<>();
        levelRepository.findByOrganizationIdOrderByLevelRank(orgId).forEach(l -> ranks.put(l.getNodeType(), l.getLevelRank()));
        return ranks;
    }

    private static int rankOf(Map<String, Integer> ranks, String type) {
        Integer rank = ranks.get(type);
        if (rank == null) {
            throw new IllegalArgumentException("Unknown level type: " + type);
        }
        return rank;
    }

    private static void assertNameFree(Collection<OrgNode> all, Long parentId, String name, Long exceptId) {
        boolean taken = all.stream().anyMatch(n -> parentId.equals(n.getParentId())
            && n.getName().equalsIgnoreCase(name) && !n.getId().equals(exceptId));
        if (taken) {
            throw new DuplicateResourceException("A node named " + name + " already exists under this parent");
        }
    }

    private static String cleanName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (trimmed.length() > 150) {
            throw new IllegalArgumentException("Name is limited to 150 characters");
        }
        return trimmed;
    }

    static String normaliseType(String raw) {
        String type = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
        if (!TYPE_PATTERN.matcher(type).matches()) {
            throw new IllegalArgumentException("Type is required and may contain letters, digits and underscores only");
        }
        return type;
    }

    private static String blankToNull(String value, int max, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException(field + " is limited to " + max + " characters");
        }
        return trimmed;
    }

    private static String label(String type) {
        return prettify(type);
    }

    static String prettify(String type) {
        String lower = type.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String nameOf(Map<Long, OrgNode> byId, Long id) {
        OrgNode n = id == null ? null : byId.get(id);
        return n == null ? null : n.getName();
    }

    private static String cell(List<String> row, int index) {
        return index < 0 || index >= row.size() ? "" : row.get(index).trim();
    }

    private void audit(String action, Long customerId, Long orgId, OrgNode node, String detail) {
        auditService.recordSuccess(action, null, customerId, null, "OrgNode", String.valueOf(node.getId()), orgId, detail);
    }

    /** Minimal RFC 4180 reader: quoted fields, doubled quotes, CRLF, BOM; blank lines are skipped. */
    static List<List<String>> parseCsv(String text) {
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean any = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                quoted = true;
                any = true;
            } else if (c == ',') {
                row.add(field.toString());
                field.setLength(0);
                any = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                if (any || field.length() > 0) {
                    row.add(field.toString());
                    rows.add(row);
                }
                row = new ArrayList<>();
                field.setLength(0);
                any = false;
            } else {
                field.append(c);
                any = true;
            }
        }
        if (any || field.length() > 0) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }
}
