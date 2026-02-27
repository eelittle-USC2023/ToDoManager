package com.example.todomanager.service;

import com.example.todomanager.model.Organization;
import com.example.todomanager.model.Role;
import com.example.todomanager.model.User;
import com.example.todomanager.repository.OrganizationRepository;
import com.example.todomanager.repository.RoleRepository;
import com.example.todomanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoleService {

    private final RoleRepository roleRepo;
    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;

    public RoleService(RoleRepository roleRepo, OrganizationRepository orgRepo, UserRepository userRepo) {
        this.roleRepo = roleRepo;
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
    }

    /**
     * Create a role. actingUserId must be owner of the organization.
     * Enforces supervisor role (must be within same organization or null for Owner)
     */
    @Transactional
    public Role create(Role role, UUID actingUserId) {
        if (role == null || role.getOrganization() == null || role.getOrganization().getId() == null) {
            throw new BusinessException("role.organization.id is required");
        }
        Organization org = orgRepo.findById(role.getOrganization().getId())
                .orElseThrow(() -> new BusinessException("organization not found"));

        // only owner may create roles
        if (org.getOwner() == null || !org.getOwner().getId().equals(actingUserId)) {
            throw new BusinessException("only organization owner may create roles");
        }

        // validate supervisor role is in same organization or null (Owner role must have null supervisor)
        if (role.getSupervisorRole() != null && role.getSupervisorRole().getId() != null) {
            Role sup = roleRepo.findById(role.getSupervisorRole().getId()).orElseThrow(() -> new BusinessException("supervisor role not found"));
            if (sup.getOrganization() == null || !sup.getOrganization().getId().equals(org.getId())) {
                throw new BusinessException("supervisor role must be in the same organization");
            }
            role.setSupervisorRole(sup);
        } else {
            // OK to be null (e.g., Owner)
            role.setSupervisorRole(null);
        }

        // validate users if present and attach managed users
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            Set<User> managed = new HashSet<User>();
            for (User u : role.getUsers()) {
                if (u.getId() == null) throw new BusinessException("user id required for role membership");
                User mu = userRepo.findById(u.getId()).orElseThrow(() -> new BusinessException("user not found: " + u.getId()));
                managed.add(mu);
            }
            role.setUsers(managed);
        } else {
            role.setUsers(new HashSet<User>());
        }

        role.setOrganization(org);
        return roleRepo.save(role);
    }

    @Transactional
    public Role edit(Role role, UUID actingUserId) {
        if (role == null || role.getId() == null) throw new BusinessException("role id required for edit");
        Role existing = roleRepo.findById(role.getId()).orElseThrow(() -> new BusinessException("role not found"));

        Organization org = existing.getOrganization();
        if (org == null) throw new BusinessException("role missing organization");
        // only owner may edit
        if (org.getOwner() == null || !org.getOwner().getId().equals(actingUserId)) {
            throw new BusinessException("only organization owner may edit roles");
        }

        if (role.getTitle() != null) existing.setTitle(role.getTitle());
        existing.setWorkDays(role.getWorkDays());
        existing.setWorkHours(role.getWorkHours());
        existing.setHoursPerWeek(role.getHoursPerWeek());

        // supervisor validation
        if (role.getSupervisorRole() != null && role.getSupervisorRole().getId() != null) {
            Role sup = roleRepo.findById(role.getSupervisorRole().getId()).orElseThrow(() -> new BusinessException("supervisor role not found"));
            if (sup.getOrganization() == null || !sup.getOrganization().getId().equals(org.getId())) {
                throw new BusinessException("supervisor role must be in the same organization");
            }
            existing.setSupervisorRole(sup);
        } else {
            existing.setSupervisorRole(null);
        }

        // users update
        if (role.getUsers() != null) {
            Set<User> managed = new HashSet<User>();
            for (User u : role.getUsers()) {
                if (u.getId() == null) throw new BusinessException("user id required for role membership");
                User mu = userRepo.findById(u.getId()).orElseThrow(() -> new BusinessException("user not found: " + u.getId()));
                managed.add(mu);
            }
            existing.setUsers(managed);
        }

        return roleRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public Role get(UUID id, UUID actingUserId) {
        Role role = roleRepo.findById(id).orElseThrow(() -> new BusinessException("role not found"));
        // acting user must have a role in this organization
        if (!userHasRoleInOrganization(actingUserId, role.getOrganization() != null ? role.getOrganization().getId() : null)) {
            throw new BusinessException("user does not have access to this role");
        }
        return role;
    }

    @Transactional
    public void delete(UUID id, UUID actingUserId) {
        Role role = roleRepo.findById(id).orElseThrow(() -> new BusinessException("role not found"));
        Organization org = role.getOrganization();
        if (org == null || org.getOwner() == null || !org.getOwner().getId().equals(actingUserId)) {
            throw new BusinessException("only organization owner may delete role");
        }
        roleRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Set<User> getAllUsers(UUID roleId) {
        Role role = roleRepo.findById(roleId).orElseThrow(() -> new BusinessException("role not found"));
        if (role.getUsers() == null) return new HashSet<User>();
        return new HashSet<User>(role.getUsers());
    }

    private boolean userHasRoleInOrganization(UUID userId, UUID orgId) {
        if (userId == null || orgId == null) return false;
        List<Role> roles = roleRepo.findByUsers_Id(userId);
        for (Role r : roles) {
            if (r.getOrganization() != null && r.getOrganization().getId().equals(orgId)) return true;
        }
        return false;
    }
}