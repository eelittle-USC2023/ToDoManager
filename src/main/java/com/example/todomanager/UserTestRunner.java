package com.example.todomanager;

import com.example.todomanager.model.User;
import com.example.todomanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserTestRunner implements CommandLineRunner {

    private final UserService userService;

    public UserTestRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("----- USER SERVICE TEST START -----");

        // 1️⃣ Create user
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPassword("password123");

        User saved = userService.create(newUser);
        System.out.println("Created user: " + saved.getId());

        UUID userId = saved.getId();

        // 2️⃣ Get user by ID
        User found = userService.get(userId);
        System.out.println("Found user: " + found.getUsername());

        // 3️⃣ Test login
        boolean loginSuccess = userService.login("testuser", "password123");
        System.out.println("Login success (correct password): " + loginSuccess);

        boolean loginFail = userService.login("testuser", "wrongpassword");
        System.out.println("Login success (wrong password): " + loginFail);

        // 4️⃣ Edit user
        found.setUsername("updatedUser");
        User updated = userService.edit(found);
        System.out.println("Updated username: " + updated.getUsername());

        // 5️⃣ Delete user
        userService.delete(userId);
        System.out.println("User deleted.");

        // 6️⃣ Confirm deletion
        try {
            userService.get(userId);
        } catch (Exception e) {
            System.out.println("Confirmed deletion: user no longer exists.");
        }

        System.out.println("----- USER SERVICE TEST END -----");
    }
}