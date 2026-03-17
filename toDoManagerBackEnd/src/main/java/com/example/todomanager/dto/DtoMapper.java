package com.example.todomanager.dto;

import com.example.todomanager.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class DtoMapper {

    // ---------- User ----------
    public static UserResponse toUserResponse(User u) {
        if (u == null) return null;
        return new UserResponse(u.getId(), u.getUsername());
    }

    public static User fromCreateUserRequest(CreateUserRequest r) {
        User u = new User();
        // id left null — service may set it
        u.setUsername(r.username());
        u.setPassword(r.password());
        return u;
    }

    public static void applyUpdateUserRequest(User target, UpdateUserRequest r) {
        if (r.username() != null) target.setUsername(r.username());
        if (r.password() != null) target.setPassword(r.password());
    }

    // ---------- Organization ----------
    // Organization -> OrganizationResponse
    public static OrganizationResponse toOrganizationResponse(Organization o) {
        if (o == null) return null;
        User owner = (o.getOwner() == null ? null : o.getOwner());
        if (owner == null) return null;
        User ownerRes = new User();
        ownerRes.setId(owner.getId());
        ownerRes.setUsername(owner.getUsername());
        return new OrganizationResponse(o.getId(), o.getName(), ownerRes);
    }

    // CreateOrganizationRequest -> Organization (minimal entity)
    public static Organization fromCreateOrganizationRequest(CreateOrganizationRequest req) {
        Organization org = new Organization();
        // id left null to be set by service if desired
        org.setName(req.name());
        if (req.ownerId() != null) {
            User owner = new User();
            owner.setId(req.ownerId());
            org.setOwner(owner);
        }
        return org;
    }

    // Apply updates from UpdateOrganizationRequest onto an existing (possibly minimal) Organization
    public static void applyUpdateOrganizationRequest(Organization target, UpdateOrganizationRequest req) {
        if (req == null) return;
        if (req.name() != null) target.setName(req.name());
    }

    // ---------- Role ----------
    public static RoleResponse toRoleResponse(Role r) {
        if (r == null) return null;
        UUID sup = r.getSupervisorRole() == null ? null : r.getSupervisorRole().getId();
        UUID orgId = r.getOrganization() == null ? null : r.getOrganization().getId();
        Set<UUID> userIds = (r.getUsers() == null)
                ? Collections.emptySet()
                : r.getUsers().stream().map(User::getId).collect(Collectors.toSet());

        Double hours = null;
        try {
            java.lang.reflect.Method m = r.getClass().getMethod("getHoursPerWeek");
            Object val = m.invoke(r);
            if (val instanceof java.math.BigDecimal) {
                hours = ((java.math.BigDecimal) val).doubleValue();
            } else if (val instanceof Double) {
                hours = (Double) val;
            }
        } catch (Exception ignore) {
            // fallback: no hours
        }

        return new RoleResponse(r.getId(), r.getTitle(), orgId, sup, userIds, r.getWorkHours(), r.getWorkDays(), hours);
    }

    public static Role fromCreateRoleRequest(CreateRoleRequest req) {
        Role role = new Role();
        role.setTitle(req.title());
        if (req.organizationId() != null) {
            Organization org = new Organization();
            org.setId(req.organizationId());
            role.setOrganization(org);
        }
        if (req.supervisorRoleId() != null) {
            Role sup = new Role();
            sup.setId(req.supervisorRoleId());
            role.setSupervisorRole(sup);
        }
        if (req.userIds() != null) {
            Set<User> users = req.userIds().stream().map(id -> {
                User u = new User();
                u.setId(id);
                return u;
            }).collect(Collectors.toSet());
            role.setUsers(users);
        } else {
            role.setUsers(new HashSet<>());
        }
        role.setWorkHours(req.workHours());
        role.setWorkDays(req.workDays());
        // set hours per week accordingly if entity uses Double or BigDecimal
        try {
            java.lang.reflect.Method setter = role.getClass().getMethod("setHoursPerWeek", Double.class);
            setter.invoke(role, req.hoursPerWeek());
        } catch (NoSuchMethodException nsme) {
            // try BigDecimal setter
            try {
                java.lang.reflect.Method setter2 = role.getClass().getMethod("setHoursPerWeek", java.math.BigDecimal.class);
                setter2.invoke(role, req.hoursPerWeek() == null ? null : java.math.BigDecimal.valueOf(req.hoursPerWeek()));
            } catch (Exception ignore) {}
        } catch (Exception ignore) {}

        return role;
    }

    public static void applyUpdateRoleRequest(com.example.todomanager.model.Role target,
                                            com.example.todomanager.dto.UpdateRoleRequest req) {
        if (req == null) return;

        // Title (String): null => unchanged, "" => clear, non-empty => set
        if (req.title() != null) {
            target.setTitle(req.title().isEmpty() ? null : req.title());
        }

        // Supervisor role: explicit clear flag, or set when id provided
        if (req.clearSupervisorRole()) {
            target.setSupervisorRole(null);
        } else if (req.supervisorRoleId() != null) {
            com.example.todomanager.model.Role sup = new com.example.todomanager.model.Role();
            sup.setId(req.supervisorRoleId());
            target.setSupervisorRole(sup);
        }

        // Users: explicit clear flag (clearUsers) takes precedence; otherwise set when userIds provided
        if (req.clearUsers()) {
            target.setUsers(new HashSet<>());
        } else if (req.userIds() != null) {
            Set<com.example.todomanager.model.User> users = req.userIds().stream().map(id -> {
                com.example.todomanager.model.User u = new com.example.todomanager.model.User();
                u.setId(id);
                return u;
            }).collect(Collectors.toSet());
            target.setUsers(users);
        }

        // workHours (String): null => unchanged, "" => clear, non-empty => set
        if (req.workHours() != null) {
            target.setWorkHours(req.workHours().isEmpty() ? null : req.workHours());
        }

        // workDays (String): same semantics as workHours
        if (req.workDays() != null) {
            target.setWorkDays(req.workDays().isEmpty() ? null : req.workDays());
        }

        // hoursPerWeek (Double): if non-null, set; if null => leave unchanged
        if (req.hoursPerWeek() != null) {
            target.setHoursPerWeek(req.hoursPerWeek());
        }
    }

    // ---- TaskFolder mapping ----
    public static TaskFolderResponse toTaskFolderResponse(TaskFolder f) {
        if (f == null) return null;
        java.util.UUID userId = (f.getUser() == null ? null : f.getUser().getId());
        return new TaskFolderResponse(f.getId(), userId, f.getTitle(), f.getNote());
    }

    public static TaskFolder fromCreateTaskFolderRequest(CreateTaskFolderRequest req) {
        TaskFolder f = new TaskFolder();
        f.setTitle(req.title());
        f.setNote(req.note());
        if (req.userId() != null) {
            User u = new User();
            u.setId(req.userId());
            f.setUser(u);
        }
        return f;
    }

    public static void applyUpdateTaskFolderRequest(TaskFolder target, UpdateTaskFolderRequest req) {
        if (req == null) return;
        if (req.title() != null) target.setTitle(req.title());
        if (req.note() != null) target.setNote(req.note());
    }

    // ---- Task mapping ----
    public static TaskResponse toTaskResponse(Task t) {
        if (t == null) return null;
        java.util.UUID userId = t.getUser() == null ? null : t.getUser().getId();
        java.util.UUID folderId = t.getFolder() == null ? null : t.getFolder().getId();
        java.util.UUID parentId = t.getParentTask() == null ? null : t.getParentTask().getId();
        return new TaskResponse(
                t.getId(),
                userId,
                t.getTitle(),
                t.getDescription(),
                t.getStartDateTime(),
                t.getDueOffsetHours(),
                t.getRecurrenceFrequencyHours(),
                t.getTimeToCompleteMinutes(),
                folderId,
                parentId
        );
    }

    public static Task fromCreateTaskRequest(CreateTaskRequest req) {
        Task t = new Task();
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setUser(req.user());
        t.setStartDateTime(req.startDateTime());
        t.setDueOffsetHours(req.dueOffsetHours());
        t.setRecurrenceFrequencyHours(req.recurrenceFrequencyHours());
        t.setTimeToCompleteMinutes(req.timeToCompleteMinutes());

        if (req.associatedFolderId() != null) {
            TaskFolder tf = new TaskFolder();
            tf.setId(req.associatedFolderId());
            t.setFolder(tf);
        } else {
            t.setFolder(null);
        }

        if (req.parentTaskId() != null) {
            Task parent = new Task();
            parent.setId(req.parentTaskId());
            t.setParentTask(parent);
        } else {
            t.setParentTask(null);
        }
        return t;
    }

    public static void applyUpdateTaskRequest(Task target, UpdateTaskRequest req) {

        if (req.title() != null) {
            target.setTitle(req.title().isEmpty() ? null : req.title());
        }

        if (req.description() != null) {
            target.setDescription(req.description().isEmpty() ? null : req.description());
        }

        if (req.startDateTime() != null) {
            target.setStartDateTime(req.startDateTime());
        }

        if (req.dueOffsetHours() != null) {
            target.setDueOffsetHours(req.dueOffsetHours());
        }

        if (req.recurrenceFrequencyHours() != null) {
            target.setRecurrenceFrequencyHours(req.recurrenceFrequencyHours());
        }

        if (req.timeToCompleteMinutes() != null) {
            target.setTimeToCompleteMinutes(req.timeToCompleteMinutes());
        }

        if (req.clearAssociatedFolder()) {
            target.setFolder(null);
        } else if (req.associatedFolderId() != null) {
            TaskFolder folder = new TaskFolder();
            folder.setId(req.associatedFolderId());
            target.setFolder(folder);
        }

        if (req.clearParentTask()) {
            target.setParentTask(null);
        } else if (req.parentTaskId() != null) {
            Task parent = new Task();
            parent.setId(req.parentTaskId());
            target.setParentTask(parent);
        }
    }
}