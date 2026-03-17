package com.example.todomanager.controller;

import com.example.todomanager.dto.*;
import com.example.todomanager.model.Organization;
import com.example.todomanager.service.OrganizationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService orgService;

    public OrganizationController(OrganizationService orgService) {
        this.orgService = orgService;
    }

    /**
     * Create an organization.
     * The request body should be CreateOrganizationRequest(name, ownerId).
     * ownerId is required because orgService.create(...) currently requires the creatingUserId.
     */
    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@RequestBody CreateOrganizationRequest req) {
        Organization org = com.example.todomanager.dto.DtoMapper.fromCreateOrganizationRequest(req);
        Organization saved = orgService.create(org, req.ownerId());
        OrganizationResponse resp = com.example.todomanager.dto.DtoMapper.toOrganizationResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> get(@PathVariable UUID id,
                                                    @RequestParam("actingUserId") UUID actingUserId) {
        Organization o = orgService.get(id, actingUserId);
        return ResponseEntity.ok(com.example.todomanager.dto.DtoMapper.toOrganizationResponse(o));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> edit(@PathVariable UUID id,
                                                     @RequestParam("actingUserId") UUID actingUserId,
                                                     @RequestBody UpdateOrganizationRequest req) {
        Organization org = new Organization();
        org.setId(id);
        com.example.todomanager.dto.DtoMapper.applyUpdateOrganizationRequest(org, req);
        Organization updated = orgService.edit(org, actingUserId);
        return ResponseEntity.ok(com.example.todomanager.dto.DtoMapper.toOrganizationResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestParam("actingUserId") UUID actingUserId) {
        orgService.delete(id, actingUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<List<com.example.todomanager.dto.RoleResponse>> getAllRoles(
            @PathVariable UUID id,
            @RequestParam("actingUserId") UUID actingUserId) {
        var roles = orgService.getAllRoles(id, actingUserId);
        var resp = roles.stream().map(com.example.todomanager.dto.DtoMapper::toRoleResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }
}