package uz.pdp.appclickup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.appclickup.entity.User;
import uz.pdp.appclickup.entity.Workspace;
import uz.pdp.appclickup.entity.WorkspaceRole;
import uz.pdp.appclickup.entity.WorkspaceUser;
import uz.pdp.appclickup.payload.*;
import uz.pdp.appclickup.security.CurrentUser;
import uz.pdp.appclickup.service.WorkspaceService;

import javax.management.relation.Role;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {
    @Autowired
    WorkspaceService workspaceService;

    @PostMapping
    public HttpEntity<?> addWorkspace(@Valid @RequestBody WorkspaceDTO workspaceDTO, @CurrentUser User user) {
        ApiResponse apiResponse = workspaceService.addWorkspace(workspaceDTO, user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * NAME, COLOR, AVATAR O'ZGARAISHI MUMKIN
     *
     * @param id
     * @param workspaceDTO
     * @return
     */
    @PutMapping("/{id}")
    public HttpEntity<?> editWorkspace(@PathVariable Long id, @RequestBody WorkspaceDTO workspaceDTO) {
        ApiResponse apiResponse = workspaceService.editWorkspace(id, workspaceDTO);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * @param id
     * @param ownerId
     * @return
     */
    @PutMapping("/changeOwner/{id}")
    public HttpEntity<?> changeOwnerWorkspace(@PathVariable Long id,
                                              @RequestParam UUID ownerId) {
        ApiResponse apiResponse = workspaceService.changeOwnerWorkspace(id, ownerId);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


    /**
     * ISHXONANI O'CHIRISH
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public HttpEntity<?> deleteWorkspace(@PathVariable Long id) {
        ApiResponse apiResponse = workspaceService.deleteWorkspace(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @PostMapping("/addOrEditOrRemove/{id}")
    public HttpEntity<?> addOrEditOrRemoveWorkspace(@PathVariable Long id,
                                                    @RequestBody MemberDTO memberDTO) {
        ApiResponse apiResponse = workspaceService.addOrEditOrRemoveWorkspace(id, memberDTO);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @PutMapping("/join")
    public HttpEntity<?> joinToWorkspace(@RequestParam Long id,
                                         @CurrentUser User user) {
        ApiResponse apiResponse = workspaceService.joinToWorkspace(id, user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * workspace id orqali shu ishxonadagi member va guest lar ro'yxati
     * @param id
     * @return
     */
    @GetMapping("/getMembers/{id}")
    public HttpEntity<?> getMembersList(@PathVariable Long id) {
        List<User> membersList = workspaceService.getMembersList(id);
        return ResponseEntity.ok(membersList);
    }

    /**
     * user ning workspacelari ro'yxatini olish
     * @param user sistemadadagi user
     * @return
     */
    @GetMapping("/getAll")
    public HttpEntity<?> getWorkspaces(@CurrentUser User user) {
        List<Workspace> workspaceList = workspaceService.getWorkspaces(user);
        return ResponseEntity.ok(workspaceList);
    }


    /**
     * workspacega yangi rol qo'shish
     * @param id role qo'shiladigan workspace id si
     * @param roleDTO yangi role nomi va qaysi roldan extend olganligi keladi DTO da
     * @return
     */
    @PostMapping("/addRole/{id}")
    public HttpEntity<?> addRoleToWorkspace(@PathVariable Long id, @RequestBody RoleDTO roleDTO){
        ApiResponse apiResponse=workspaceService.addRoleToWorkspace(id,roleDTO);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


    /**
     * role ga huquqlarni berish va olish
     * @param workspaceRoleDTO
     * @return
     */
    @PutMapping("/addOrRemovePermission")
    public HttpEntity<?> addOrRemovePermissionToRole(@RequestBody WorkspaceRoleDTO workspaceRoleDTO){
        ApiResponse apiResponse=workspaceService.addOrRemovePermissionToRole(workspaceRoleDTO);
        return ResponseEntity.status(apiResponse.isSuccess()?200:409).body(apiResponse);
    }
}
