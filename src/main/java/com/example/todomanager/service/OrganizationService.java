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
public class OrganizationService {

    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public OrganizationService(OrganizationRepository orgRepo, UserRepository userRepo, RoleRepository roleRepo) {
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    /**
     * Create an organization. The creatingUserId becomes owner.
     * Also creates an "Owner" role in the organization (no supervisor) and assigns the creating user to it.
     */
    @Transactional
    public Organization create(Organization organization, UUID creatingUserId) {
        if (organization == null) throw new BusinessException("organization is null");
        if (creatingUserId == null) throw new BusinessException("creating user id required");
        User owner = userRepo.findById(creatingUserId).orElseThrow(() -> new BusinessException("creating user not found"));

        if (organization.getId() == null) organization.setId(UUID.randomUUID());
        organization.setOwner(owner);

        Organization saved = orgRepo.save(organization);

        // create Owner role with no supervisor and assign owner to it
        Role ownerRole = new Role();
        ownerRole.setTitle("Owner");
        ownerRole.setOrganization(saved);
        ownerRole.setSupervisorRole(null);
        ownerRole.getUsers().add(owner);
        ownerRole.setWorkDays(null);
        ownerRole.setWorkHours(null);
        ownerRole.setHoursPerWeek(null);

        roleRepo.save(ownerRole);

        return saved;
    }

    @Transactional
    public Organization edit(Organization organization, UUID actingUserId) {
        if (organization == null || organization.getId() == null) throw new BusinessException("organization id required for edit");
        Organization existing = orgRepo.findById(organization.getId()).orElseThrow(() -> new BusinessException("organization not found"));

        // only owner can edit
        if (existing.getOwner() == null || !existing.getOwner().getId().equals(actingUserId)) {
            throw new BusinessException("only owner may edit organization");
        }

        if (organization.getName() != null) existing.setName(organization.getName());
        // copy other editable fields if present
        return orgRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public Organization get(UUID id, UUID actingUserId) {
        Organization org = orgRepo.findById(id).orElseThrow(() -> new BusinessException("organization not found"));
        // acting user must have a role in this organization to view
        if (!userHasRoleInOrganization(actingUserId, id)) {
            throw new BusinessException("user does not have access to this organization");
        }
        return org;
    }

    @Transactional
    public void delete(UUID id, UUID actingUserId) {
        Organization org = orgRepo.findById(id).orElseThrow(() -> new BusinessException("organization not found"));
        if (org.getOwner() == null || !org.getOwner().getId().equals(actingUserId)) {
            throw new BusinessException("only owner may delete organization");
        }
        orgRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles(UUID orgId, UUID actingUserId) {
        // acting user must have a role in the organization
        if (!userHasRoleInOrganization(actingUserId, orgId)) {
            throw new BusinessException("user does not have access to this organization");
        }
        // find roles by organization. Assuming RoleRepository has `findByOrganization_Id(UUID orgId)`
        List<Role> roles = roleRepo.findByOrganization_Id(orgId);
        if (roles == null) return Collections.emptyList();
        return roles;
    }

    // helper: checks whether user has any role in the organization
    private boolean userHasRoleInOrganization(UUID userId, UUID orgId) {
        if (userId == null) return false;
        List<Role> roles = roleRepo.findByUsers_Id(userId);
        for (Role r : roles) {
            if (r.getOrganization() != null && r.getOrganization().getId().equals(orgId)) return true;
        }
        return false;
    }
}