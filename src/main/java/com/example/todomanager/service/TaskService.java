package com.example.todomanager.service;

import com.example.todomanager.model.Role;
import com.example.todomanager.model.Task;
import com.example.todomanager.model.TaskFolder;
import com.example.todomanager.model.User;
import com.example.todomanager.repository.RoleRepository;
import com.example.todomanager.repository.TaskRepository;
import com.example.todomanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final TaskFolderService folderService;

    public TaskService(TaskRepository taskRepo, UserRepository userRepo, RoleRepository roleRepo, TaskFolderService folderService) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.folderService = folderService;
    }

    /**
     * Create a Task. creatingUserId indicates who is creating this task.
     * The creation is allowed only if the creating user is either the assigned user or is a supervisor (direct or transitive) of the assigned user.
     */
    @Transactional
    public Task create(Task task, UUID creatingUserId) {
        if (task == null) throw new BusinessException("task is null");
        if (task.getUser() == null || task.getUser().getId() == null) throw new BusinessException("task must include assigned user id");
        UUID assignedUserId = task.getUser().getId();

        // ensure assigned user exists
        User assigned = userRepo.findById(assignedUserId).orElseThrow(() -> new BusinessException("assigned user not found"));

        // check permission: creatingUser must be the assigned user or a supervisor above them
        if (creatingUserId == null) throw new BusinessException("creating user id required");
        if (!creatingUserId.equals(assignedUserId) && !isSupervisorOf(creatingUserId, assignedUserId)) {
            throw new BusinessException("creating user is not authorized to create a task for this assigned user");
        }

        // resolve folder if provided
        if (task.getFolder() != null && task.getFolder().getId() != null) {
            TaskFolder folder = folderService.get(task.getFolder().getId());
            task.setFolder(folder);
        }

        // set created id if missing
        if (task.getId() == null) task.setId(UUID.randomUUID());

        // ensure assigned user attached as managed entity
        task.setUser(assigned);

        return taskRepo.save(task);
    }

    @Transactional
    public Task edit(Task task, UUID actingUserId) {
        if (task == null || task.getId() == null) throw new BusinessException("task id required for edit");
        Task existing = taskRepo.findById(task.getId()).orElseThrow(() -> new BusinessException("task not found"));

        // who can edit? Not explicitly restricted in your rules except create. We'll allow any of:
        // - the assigned user
        // - a supervisor of the assigned user
        UUID assignedUserId = existing.getUser() != null ? existing.getUser().getId() : null;
        if (actingUserId == null) throw new BusinessException("acting user id required");
        if (!actingUserId.equals(assignedUserId) && !isSupervisorOf(actingUserId, assignedUserId)) {
            throw new BusinessException("acting user not authorized to edit this task");
        }

        if (task.getTitle() != null) existing.setTitle(task.getTitle());
        if (task.getDescription() != null) existing.setDescription(task.getDescription());
        if (task.getStartDateTime() != null) existing.setStartDateTime(task.getStartDateTime());
        existing.setDueOffsetHours(task.getDueOffsetHours());
        existing.setRecurrenceFrequencyHours(task.getRecurrenceFrequencyHours());
        existing.setTimeToCompleteMinutes(task.getTimeToCompleteMinutes());

        if (task.getFolder() != null && task.getFolder().getId() != null) {
            TaskFolder folder = folderService.get(task.getFolder().getId());
            existing.setFolder(folder);
        } else if (task.getFolder() == null) {
            existing.setFolder(null);
        }

        return taskRepo.save(existing);
    }

    @Transactional(readOnly = true)
    public Task get(UUID id) {
        return taskRepo.findById(id).orElseThrow(() -> new BusinessException("task not found"));
    }

    @Transactional
    public void delete(UUID id, UUID actingUserId) {
        Task existing = taskRepo.findById(id).orElseThrow(() -> new BusinessException("task not found"));
        UUID assignedUserId = existing.getUser() != null ? existing.getUser().getId() : null;
        if (actingUserId == null) throw new BusinessException("acting user id required");
        if (!actingUserId.equals(assignedUserId) && !isSupervisorOf(actingUserId, assignedUserId)) {
            throw new BusinessException("acting user not authorized to delete this task");
        }
        taskRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> getByUser(UUID userId) {
        if (userId == null) throw new BusinessException("user id required");
        List<Task> list = taskRepo.findByUser_Id(userId);
        if (list == null) return Collections.emptyList();
        return list;
    }

    /**
     * Determine whether actingUserId is a supervisor (direct or transitive) of targetUserId.
     * We look at roles assigned to the target user and climb supervisorRole chains for membership.
     */
    private boolean isSupervisorOf(UUID actingUserId, UUID targetUserId) {
        if (actingUserId == null || targetUserId == null) return false;
        // get roles containing the target user
        List<Role> targetRoles = roleRepo.findByUsers_Id(targetUserId);
        if (targetRoles == null || targetRoles.isEmpty()) return false;

        for (Role role : targetRoles) {
            Role current = role.getSupervisorRole();
            while (current != null) {
                // if any user in current role matches actingUserId -> authorized
                if (current.getUsers() != null) {
                    for (User u : current.getUsers()) {
                        if (u != null && u.getId() != null && u.getId().equals(actingUserId)) {
                            return true;
                        }
                    }
                }
                current = current.getSupervisorRole();
            }
        }
        return false;
    }
}