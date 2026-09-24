package com.vyoog.eisplatform.modules.authorization.repository;

import com.vyoog.eisplatform.modules.authorization.model.Role;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    /** The one query every permission check runs: which of the caller's role
     * names actually exist, in this scope, with their permissions loaded. */
    List<Role> findByNameInAndScope(Collection<String> names, RoleScope scope);

    /** Phase 6 (PAM): which role(s) actually grant a named permission —
     * lets a privileged-access request's {@code scope} be derived server-side
     * from the permission being requested, instead of trusting a
     * client-supplied scope value that could disagree with reality. */
    List<Role> findByPermissions_Name(String permissionName);
}
