package com.example.todomanager.service;

import com.example.todomanager.model.TaskFolder;
import com.example.todomanager.model.User;
import com.example.todomanager.repository.TaskFolderRepository;
import com.example.todomanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final TaskFolderRepository folderRepo;

    public UserService(UserRepository userRepo, TaskFolderRepository folderRepo) {
        this.userRepo = userRepo;
        this.folderRepo = folderRepo;
    }

    /**
     * Create a user. When created, automatically creates a "Completed Tasks" folder for that user.
     */
    @Transactional
    public User create(User user) {
        if (user == null) throw new BusinessException("user is null");
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new BusinessException("username and password are required");
        }
        // Check username uniqueness (assumes findByUsername returns Optional<User>)
        Optional<User> existing = userRepo.findByUsername(user.getUsername());
        if (existing.isPresent()) {
            throw new BusinessException("username already exists");
        }
        // assign id if missing
        if (user.getId() == null) user.setId(UUID.randomUUID());
        User saved = userRepo.save(user);

        // create default Completed Tasks folder
        TaskFolder folder = new TaskFolder();
        folder.setId(UUID.randomUUID());
        folder.setUser(saved);
        folder.setTitle("Completed Tasks");
        folder.setNote("Default completed tasks folder");
        folderRepo.save(folder);

        return saved;
    }

    @Transactional
    public User edit(User user) {
        if (user == null || user.getId() == null) throw new BusinessException("user id required for edit");
        User existing = userRepo.findById(user.getId()).orElseThrow(() -> new BusinessException("user not found"));
        if (user.getUsername() != null) existing.setUsername(user.getUsername());
        if (user.getPassword() != null) existing.setPassword(user.getPassword());
        // copy other editable fields as needed
        return userRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public User get(UUID id) {
        return userRepo.findById(id).orElseThrow(() -> new BusinessException("user not found"));
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepo.existsById(id)) throw new BusinessException("user not found");
        userRepo.deleteById(id);
    }

    /**
     * Returns true if the username/password match a stored user.
     * NOTE: This is a simple check. For production, use hashed passwords and a secure auth flow.
     */
    @Transactional(readOnly = true)
    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        Optional<User> maybe = userRepo.findByUsername(username);
        if (!maybe.isPresent()) return false;
        User u = maybe.get();
        // In production compare hashed passwords. Here we compare raw strings (as your project currently stores).
        return password.equals(u.getPassword());
    }
}