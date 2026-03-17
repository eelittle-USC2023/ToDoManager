package com.example.todomanager;

import com.example.todomanager.model.User;
import com.example.todomanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepoCommandLineTester implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserRepoCommandLineTester(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== UserRepository CLI Test ===");

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername("cli_user");
        u.setPassword("cli_pw");
        User saved = userRepository.save(u);
        System.out.println("Saved user id: " + saved.getId());

        Optional<User> found = userRepository.findById(saved.getId());
        System.out.println("Find by id present: " + found.isPresent());

        Optional<User> byName = userRepository.findByUsername("cli_user");
        System.out.println("Find by username present: " + byName.isPresent());

        userRepository.deleteById(saved.getId());
        System.out.println("Deleted user");

        System.out.println("=== CLI Test End ===");
    }
}