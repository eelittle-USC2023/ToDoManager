package com.example.todomanager.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateTaskRequest(
    String title,
    String description,
    OffsetDateTime startDateTime,
    Integer dueOffsetHours,
    Integer recurrenceFrequencyHours,
    Integer timeToCompleteMinutes,
    UUID associatedFolderId,
    boolean clearAssociatedFolder,
    UUID parentTaskId,
    boolean clearParentTask
) {}