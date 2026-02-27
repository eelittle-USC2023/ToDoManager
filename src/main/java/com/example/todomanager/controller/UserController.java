package com.example.todomanager.controller;

import com.example.todomanager.model.User;
import com.example.todomanager.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        // expects user.username and user.password to be set; service will create id and default folder
        return userService.create(user);
    }

    @PostMapping("/login")
    public boolean login(@RequestBody User user) {
        return userService.login(user.getUsername(), user.getPassword());
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable UUID id) {
        return userService.get(id);
    }

    @PutMapping("/{id}")
    public User editUser(@PathVariable UUID id, @RequestBody User user) {
        user.setId(id);
        return userService.edit(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userService.delete(id);
    }
}