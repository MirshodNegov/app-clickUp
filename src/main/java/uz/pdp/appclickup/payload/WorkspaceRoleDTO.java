package uz.pdp.appclickup.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.appclickup.entity.enums.AddType;
import uz.pdp.appclickup.entity.enums.WorkspacePermissionName;
import uz.pdp.appclickup.entity.enums.WorkspaceRoleName;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceRoleDTO {
    private UUID id;
    private String name;
    private WorkspaceRoleName extendsRole;
    private WorkspacePermissionName permissionName;
    private AddType addType;
}
