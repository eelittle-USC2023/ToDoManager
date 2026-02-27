package com.example.todomanager.controller;

import com.example.todomanager.model.Organization;
import com.example.todomanager.model.Role;
import com.example.todomanager.service.OrganizationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService orgService;

    public OrganizationController(OrganizationService orgService) {
        this.orgService = orgService;
    }

    /**
     * Create an organization.
     * The request body should be an Organization entity. The owner field may be a User with only id set.
     */
    @PostMapping
    public Organization create(@RequestBody Organization organization) {
        UUID creatingUserId = organization.getOwner() != null ? organization.getOwner().getId() : null;
        return orgService.create(organization, creatingUserId);
    }

    @GetMapping("/{id}")
    public Organization get(@PathVariable UUID id,
                            @RequestParam("actingUserId") UUID actingUserId) {
        return orgService.get(id, actingUserId);
    }

    @PutMapping("/{id}")
    public Organization edit(@PathVariable UUID id,
                             @RequestParam("actingUserId") UUID actingUserId,
                             @RequestBody Organization organization) {
        organization.setId(id);
        return orgService.edit(organization, actingUserId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id,
                       @RequestParam("actingUserId") UUID actingUserId) {
        orgService.delete(id, actingUserId);
    }

    @GetMapping("/{id}/roles")
    public List<Role> getAllRoles(@PathVariable UUID id,
                                  @RequestParam("actingUserId") UUID actingUserId) {
        return orgService.getAllRoles(id, actingUserId);
    }
}