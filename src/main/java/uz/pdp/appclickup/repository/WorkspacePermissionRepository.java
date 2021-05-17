package uz.pdp.appclickup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.appclickup.entity.WorkspacePermission;
import uz.pdp.appclickup.entity.enums.WorkspacePermissionName;

import java.util.Optional;
import java.util.UUID;

public interface WorkspacePermissionRepository extends JpaRepository<WorkspacePermission, UUID> {
    Optional<WorkspacePermission> findByWorkspaceRoleIdAndPermission(UUID workspaceRole_id, WorkspacePermissionName permission);
}
