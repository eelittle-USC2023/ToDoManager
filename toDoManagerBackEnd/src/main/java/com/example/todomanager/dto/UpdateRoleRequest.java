package com.example.todomanager.dto;

import java.util.Set;
import java.util.UUID;

public record UpdateRoleRequest(
    String title,
    UUID supervisorRoleId,
    boolean clearSupervisorRole,
    Set<UUID> userIds,
    boolean clearUsers,
    String workHours,
    String workDays,
    Double hoursPerWeek
) {}