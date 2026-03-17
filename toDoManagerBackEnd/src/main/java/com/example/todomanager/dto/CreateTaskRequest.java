package com.example.todomanager.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.todomanager.model.User;

public record CreateTaskRequest(
    User user,
    String title,
    String description,
    OffsetDateTime startDateTime,
    Integer dueOffsetHours,
    Integer recurrenceFrequencyHours,
    Integer timeToCompleteMinutes,
    UUID associatedFolderId,
    UUID parentTaskId
) {}