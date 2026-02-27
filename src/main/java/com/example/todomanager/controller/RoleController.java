package com.example.todomanager.controller;

import com.example.todomanager.model.Role;
import com.example.todomanager.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Create a Role.
     * Accepts a Role entity in the body. The organization field should have id set (Organization.id).
     * Optionally supervisorRole may have id set, and users may be a set of User objects with ids.
     */
    @PostMapping
    public Role create(@RequestParam("actingUserId") UUID actingUserId,
                       @RequestBody Role role) {
        return roleService.create(role, actingUserId);
    }

    @PutMapping("/{id}")
    public Role edit(@PathVariable UUID id,
                     @RequestParam("actingUserId") UUID actingUserId,
                     @RequestBody Role role) {
        role.setId(id);
        return roleService.edit(role, actingUserId);
    }

    @GetMapping("/{id}")
    public Role get(@PathVariable UUID id,
                    @RequestParam("actingUserId") UUID actingUserId) {
        return roleService.get(id, actingUserId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id,
                        @RequestParam("actingUserId") UUID actingUserId) {
        roleService.delete(id, actingUserId);
    }
}