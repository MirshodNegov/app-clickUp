package uz.pdp.appclickup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import uz.pdp.appclickup.entity.*;
import uz.pdp.appclickup.entity.enums.AddType;
import uz.pdp.appclickup.entity.enums.WorkspacePermissionName;
import uz.pdp.appclickup.entity.enums.WorkspaceRoleName;
import uz.pdp.appclickup.payload.*;
import uz.pdp.appclickup.repository.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    WorkspaceRepository workspaceRepository;
    @Autowired
    AttachmentRepository attachmentRepository;
    @Autowired
    WorkspaceUserRepository workspaceUserRepository;
    @Autowired
    WorkspaceRoleRepository workspaceRoleRepository;
    @Autowired
    WorkspacePermissionRepository workspacePermissionRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public ApiResponse addWorkspace(WorkspaceDTO workspaceDTO, User user) {
        //WORKSPACE OCHDIK
        if (workspaceRepository.existsByOwnerIdAndName(user.getId(), workspaceDTO.getName()))
            return new ApiResponse("Sizda bunday nomli ishxona mavjud", false);
        Workspace workspace = new Workspace(
                workspaceDTO.getName(),
                workspaceDTO.getColor(),
                user,
                workspaceDTO.getAvatarId() == null ? null : attachmentRepository.findById(workspaceDTO.getAvatarId()).orElseThrow(() -> new ResourceNotFoundException("attachment"))
        );
        workspaceRepository.save(workspace);

        //WORKSPACE ROLE OCHDIK
        WorkspaceRole ownerRole = workspaceRoleRepository.save(new WorkspaceRole(
                workspace,
                WorkspaceRoleName.ROLE_OWNER.name(),
                null
        ));
        WorkspaceRole adminRole = workspaceRoleRepository.save(new WorkspaceRole(workspace, WorkspaceRoleName.ROLE_ADMIN.name(), null));
        WorkspaceRole memberRole = workspaceRoleRepository.save(new WorkspaceRole(workspace, WorkspaceRoleName.ROLE_MEMBER.name(), null));
        WorkspaceRole guestRole = workspaceRoleRepository.save(new WorkspaceRole(workspace, WorkspaceRoleName.ROLE_GUEST.name(), null));


        //OWERGA HUQUQLARNI BERYAPAMIZ
        WorkspacePermissionName[] workspacePermissionNames = WorkspacePermissionName.values();
        List<WorkspacePermission> workspacePermissions = new ArrayList<>();

        for (WorkspacePermissionName workspacePermissionName : workspacePermissionNames) {
            WorkspacePermission workspacePermission = new WorkspacePermission(
                    ownerRole,
                    workspacePermissionName);
            workspacePermissions.add(workspacePermission);
            if (workspacePermissionName.getWorkspaceRoleNames().contains(WorkspaceRoleName.ROLE_ADMIN)) {
                workspacePermissions.add(new WorkspacePermission(
                        adminRole,
                        workspacePermissionName));
            }
            if (workspacePermissionName.getWorkspaceRoleNames().contains(WorkspaceRoleName.ROLE_MEMBER)) {
                workspacePermissions.add(new WorkspacePermission(
                        memberRole,
                        workspacePermissionName));
            }
            if (workspacePermissionName.getWorkspaceRoleNames().contains(WorkspaceRoleName.ROLE_GUEST)) {
                workspacePermissions.add(new WorkspacePermission(
                        guestRole,
                        workspacePermissionName));
            }

        }
        workspacePermissionRepository.saveAll(workspacePermissions);

        //WORKSPACE USER OCHDIK
        workspaceUserRepository.save(new WorkspaceUser(
                workspace,
                user,
                ownerRole,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis())

        ));

        return new ApiResponse("Ishxona saqlandi", true);
    }

    @Override
    public ApiResponse editWorkspace(Long id, WorkspaceDTO workspaceDTO) {
        Optional<Workspace> optionalWorkspace = workspaceRepository.findById(id);
        if (!optionalWorkspace.isPresent())
            return new ApiResponse("Workspace not found !", false);
        Workspace workspace = optionalWorkspace.get();
        workspace.setName(workspaceDTO.getName());
        workspace.setColor(workspaceDTO.getColor());
        workspace.setAvatar(attachmentRepository.findById(workspaceDTO.getAvatarId()).orElseGet(null));
        workspaceRepository.save(workspace);
        return new ApiResponse("Workspace edited successfully !", true);
    }

    @Override
    public ApiResponse changeOwnerWorkspace(Long id, UUID ownerId) {
        Optional<Workspace> optionalWorkspace = workspaceRepository.findById(id);
        if (!optionalWorkspace.isPresent())
            return new ApiResponse("Workspace not found !", false);
        Workspace workspace = optionalWorkspace.get();
        Optional<User> optionalUser = userRepository.findById(ownerId);
        if (!optionalUser.isPresent())
            return new ApiResponse("User not found !", false);
        User newOwner = optionalUser.get();
        workspace.setOwner(newOwner);
        workspaceRepository.save(workspace);
        return new ApiResponse("Workspace owner edited !", true);
    }

    @Override
    public ApiResponse deleteWorkspace(Long id) {
        try {
            workspaceRepository.deleteById(id);
            return new ApiResponse("O'chirildi", true);
        } catch (Exception e) {
            return new ApiResponse("Xatolik", false);
        }
    }


    @Override
    public ApiResponse addOrEditOrRemoveWorkspace(Long id, MemberDTO memberDTO) {
        if (memberDTO.getAddType().equals(AddType.ADD)) {
            WorkspaceUser workspaceUser = new WorkspaceUser(
                    workspaceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("id")),
                    userRepository.findById(memberDTO.getId()).orElseThrow(() -> new ResourceNotFoundException("id")),
                    workspaceRoleRepository.findById(memberDTO.getRoleId()).orElseThrow(() -> new ResourceNotFoundException("id")),
                    new Timestamp(System.currentTimeMillis()),
                    null
            );
            workspaceUserRepository.save(workspaceUser);

            //TODO EMAILGA INVITE XABAR YUBORISH
        } else if (memberDTO.getAddType().equals(AddType.EDIT)) {
            WorkspaceUser workspaceUser = workspaceUserRepository.findByWorkspaceIdAndUserId(id, memberDTO.getId()).orElseGet(WorkspaceUser::new);
            workspaceUser.setWorkspaceRole(workspaceRoleRepository.findById(memberDTO.getRoleId()).orElseThrow(() -> new ResourceNotFoundException("id")));
            workspaceUserRepository.save(workspaceUser);
        } else if (memberDTO.getAddType().equals(AddType.REMOVE)) {
            workspaceUserRepository.deleteByWorkspaceIdAndUserId(id, memberDTO.getId());
        }
        return new ApiResponse("Muvaffaqiyatli", true);
    }


    @Override
    public ApiResponse joinToWorkspace(Long id, User user) {
        Optional<WorkspaceUser> optionalWorkspaceUser = workspaceUserRepository.findByWorkspaceIdAndUserId(id, user.getId());
        if (optionalWorkspaceUser.isPresent()) {
            WorkspaceUser workspaceUser = optionalWorkspaceUser.get();
            workspaceUser.setDateJoined(new Timestamp(System.currentTimeMillis()));
            workspaceUserRepository.save(workspaceUser);
            return new ApiResponse("Success", true);
        }
        return new ApiResponse("Error", false);
    }


    @Override
    public List<User> getMembersList(Long id) {
        List<WorkspaceUser> userList = workspaceUserRepository.findAllByWorkspaceId(id);
        List<User> membersList = new ArrayList<>();
        for (WorkspaceUser workspaceUser : userList) {
            User user = workspaceUser.getUser();
            membersList.add(user);
        }
        return membersList;
    }


    @Override
    public List<Workspace> getWorkspaces(User user) {
        List<WorkspaceUser> list = workspaceUserRepository.findAllByUserId(user.getId());
        List<Workspace> workspaceList = new ArrayList<>();
        for (WorkspaceUser workspaceUser : list) {
            Workspace workspace = workspaceUser.getWorkspace();
            workspaceList.add(workspace);
        }
        return workspaceList;
    }


    @Override
    public ApiResponse addRoleToWorkspace(Long id, RoleDTO roleDTO) {
        Optional<Workspace> optionalWorkspace = workspaceRepository.findById(id);
        if (!optionalWorkspace.isPresent())
            return new ApiResponse("Workspace not found !", false);
        Workspace workspace = optionalWorkspace.get();
        WorkspaceRole workspaceRole=new WorkspaceRole();
        workspaceRole.setWorkspace(workspace);
        workspaceRole.setName(roleDTO.getName());
        WorkspacePermissionName[] workspacePermissionNames = WorkspacePermissionName.values();
        List<WorkspacePermission> workspacePermissions = new ArrayList<>();
        if (roleDTO.getId()==1){
            workspaceRole.setExtendsRole(WorkspaceRoleName.ROLE_OWNER);
            for (WorkspacePermissionName workspacePermissionName : workspacePermissionNames) {
                WorkspacePermission workspacePermission = new WorkspacePermission(
                        workspaceRole,
                        workspacePermissionName);
                workspacePermissions.add(workspacePermission);
            }
        }else if (roleDTO.getId()==2){
            workspaceRole.setExtendsRole(WorkspaceRoleName.ROLE_ADMIN);
            for (WorkspacePermissionName workspacePermissionName : workspacePermissionNames) {
                if (workspacePermissionName.getWorkspaceRoleNames().contains(WorkspaceRoleName.ROLE_ADMIN)) {
                    workspacePermissions.add(new WorkspacePermission(
                            workspaceRole,
                            workspacePermissionName));
                }
            }
        }else if (roleDTO.getId()==3){
            workspaceRole.setExtendsRole(WorkspaceRoleName.ROLE_MEMBER);
            for (WorkspacePermissionName workspacePermissionName : workspacePermissionNames) {
                if (workspacePermissionName.getWorkspaceRoleNames().contains(WorkspaceRoleName.ROLE_MEMBER)) {
                    workspacePermissions.add(new WorkspacePermission(
                            workspaceRole,
                            workspacePermissionName));
                }
            }
        }else if (roleDTO.getId()==4){
            workspaceRole.setExtendsRole(WorkspaceRoleName.ROLE_GUEST);
            for (WorkspacePermissionName workspacePermissionName : workspacePermissionNames) {
                if (workspacePermissionName.getWorkspaceRoleNames().contains(WorkspaceRoleName.ROLE_GUEST)) {
                    workspacePermissions.add(new WorkspacePermission(
                            workspaceRole,
                            workspacePermissionName));
                }
            }
        }
        workspaceRoleRepository.save(workspaceRole);
        workspacePermissionRepository.saveAll(workspacePermissions);
        return new ApiResponse("Yangi role saqlandi !",true);
    }


    @Override
    public ApiResponse addOrRemovePermissionToRole(WorkspaceRoleDTO workspaceRoleDTO) {
        WorkspaceRole workspaceRole = workspaceRoleRepository.findById(workspaceRoleDTO.getId()).orElseThrow(() -> new ResourceNotFoundException("WorkspaceRole"));
        Optional<WorkspacePermission> optionalWorkspacePermission = workspacePermissionRepository.findByWorkspaceRoleIdAndPermission(workspaceRole.getId(),workspaceRoleDTO.getPermissionName());
        if (workspaceRoleDTO.getAddType().equals(AddType.ADD)){
            if (optionalWorkspacePermission.isPresent())
                return new ApiResponse("Allaqachon qo'shilgan",false);
            WorkspacePermission workspacePermission=new WorkspacePermission(workspaceRole,workspaceRoleDTO.getPermissionName());
            workspacePermissionRepository.save(workspacePermission);
            return new ApiResponse("Permission qo'shildi",true);
        } else if (workspaceRoleDTO.getAddType().equals(AddType.REMOVE)){
            if (optionalWorkspacePermission.isPresent()){
                workspacePermissionRepository.delete(optionalWorkspacePermission.get());
                return new ApiResponse("Muvaffaqiyatli o'chirildi",true);
            }
            return new ApiResponse("Bunfay object yo'q",false);
        }
        return new ApiResponse("Bunday buyruq yo'q",false);
    }
}
