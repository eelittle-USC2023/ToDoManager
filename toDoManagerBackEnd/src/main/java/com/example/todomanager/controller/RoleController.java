package com.example.todomanager.controller;

import com.example.todomanager.dto.*;
import com.example.todomanager.model.Role;
import com.example.todomanager.model.User;
import com.example.todomanager.service.RoleService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Create a Role.
     * actingUserId is required as a request parameter (organization owner).
     */
    @PostMapping
    public ResponseEntity<RoleResponse> create(@RequestParam("actingUserId") UUID actingUserId,
                                               @RequestBody CreateRoleRequest req) {
        Role r = com.example.todomanager.dto.DtoMapper.fromCreateRoleRequest(req);
        Role saved = roleService.create(r, actingUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(com.example.todomanager.dto.DtoMapper.toRoleResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> edit(@PathVariable UUID id,
                                             @RequestParam("actingUserId") UUID actingUserId,
                                             @RequestBody UpdateRoleRequest req) {
        Role r = new Role();
        r.setId(id);
        com.example.todomanager.dto.DtoMapper.applyUpdateRoleRequest(r, req);
        Role updated = roleService.edit(r, actingUserId);
        return ResponseEntity.ok(com.example.todomanager.dto.DtoMapper.toRoleResponse(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> get(@PathVariable UUID id,
                                            @RequestParam("actingUserId") UUID actingUserId) {
        Role r = roleService.get(id, actingUserId);
        return ResponseEntity.ok(com.example.todomanager.dto.DtoMapper.toRoleResponse(r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestParam("actingUserId") UUID actingUserId) {
        roleService.delete(id, actingUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<Set<com.example.todomanager.dto.UserResponse>> getAllUsers(
            @PathVariable UUID id,
            @RequestParam("actingUserId") UUID actingUserId) {

        Set<User> users = roleService.getAllUsers(id, actingUserId);
        var resp = users.stream().map(com.example.todomanager.dto.DtoMapper::toUserResponse).collect(Collectors.toSet());
        return ResponseEntity.ok(resp);
    }
}