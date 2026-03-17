package com.example.todomanager.service;

import com.example.todomanager.model.BusinessException;
import com.example.todomanager.model.Organization;
import com.example.todomanager.model.Role;
import com.example.todomanager.model.TaskFolder;
import com.example.todomanager.model.User;
import com.example.todomanager.repository.OrganizationRepository;
import com.example.todomanager.repository.RoleRepository;
import com.example.todomanager.repository.TaskFolderRepository;
import com.example.todomanager.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final TaskFolderRepository folderRepo;
    private final RoleRepository roleRepo;
    private final OrganizationRepository orgRepo;

    public UserService(UserRepository userRepo, TaskFolderRepository folderRepo, RoleRepository roleRepo, OrganizationRepository orgRepo) {
        this.userRepo = userRepo;
        this.folderRepo = folderRepo;
        this.roleRepo = roleRepo;
        this.orgRepo =  orgRepo;
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
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations(UUID userId) {
        if (userId == null) {
            throw new BusinessException("user id is required");
        }

        // Ensure user exists
        userRepo.findById(userId).orElseThrow(() -> new BusinessException("user not found"));
        System.out.println("Loading organizations for user: " + userId);

        // Get all roles for this user
        List<Role> roles = roleRepo.findByUsers_Id(userId);

        // Use Set to prevent duplicates
        Set<Organization> organizations = new HashSet<>();

        for (Role role : roles) {
            try {
                String orgIdString = null;
                if (role.getOrganization() != null) {
                    orgIdString = role.getOrganization().getId().toString();
                }
                UUID orgId = UUID.fromString(orgIdString);
                // Log role -> org mapping for debugging
                System.out.println("Role id: " + role.getId() + " references organization id: " + orgId);

                if (orgId == null) {
                    // nothing to add
                    continue;
                }

                // Defensive: ensure organization actually exists in DB
                if (!orgRepo.existsById(orgId)) {
                    // Log the missing reference -- do not throw a raw Hibernate exception
                    System.err.println("Skipping missing organization id referenced by role " + role.getId() + ": " + orgId);
                    continue;
                }

                // Load managed organization (safe)
                Organization managed = orgRepo.findById(orgId).orElseThrow(() ->
                    new BusinessException("Organization disappeared during lookup: " + orgId));

                organizations.add(managed);
            } catch (Exception ex) {
                // Log and continue; you may choose to rethrow a BusinessException if you prefer
                System.err.println("Error processing role " + role.getId() + ": " + ex.getMessage());
            }
        }

        return new ArrayList<>(organizations);
    }
}