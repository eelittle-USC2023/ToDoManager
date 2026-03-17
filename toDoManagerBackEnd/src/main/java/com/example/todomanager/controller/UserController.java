package com.example.todomanager.controller;

import com.example.todomanager.dto.*;
import com.example.todomanager.model.User;
import com.example.todomanager.service.UserService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest req) {
        User toCreate = DtoMapper.fromCreateUserRequest(req);
        User created = userService.create(toCreate); // service sets id, default folder etc
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toUserResponse(created));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.login(request.username(), request.password());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }
        return ResponseEntity.ok(DtoMapper.toUserResponse(userOpt.get()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        User u = userService.get(id);
        return ResponseEntity.ok(DtoMapper.toUserResponse(u));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> editUser(@PathVariable UUID id, @RequestBody UpdateUserRequest req) {
        User user = new User();
        user.setId(id);
        // apply DTO values but keep logic in service
        DtoMapper.applyUpdateUserRequest(user, req);
        User updated = userService.edit(user); 
        return ResponseEntity.ok(DtoMapper.toUserResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/organizations")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations(@PathVariable UUID id) {
        List<com.example.todomanager.model.Organization> orgs = userService.getAllOrganizations(id);
        var resp = orgs.stream().map(DtoMapper::toOrganizationResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }
}