package com.example.todomanager.dto;

import java.util.Set;
import java.util.UUID;

public record CreateRoleRequest(
    String title,
    UUID organizationId,
    UUID supervisorRoleId,
    Set<UUID> userIds,
    String workHours,
    String workDays,
    Double hoursPerWeek
) {}