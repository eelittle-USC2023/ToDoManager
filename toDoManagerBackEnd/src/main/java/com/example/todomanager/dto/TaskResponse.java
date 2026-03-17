package com.example.todomanager.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID userId,
    String title,
    String description,
    OffsetDateTime startDateTime,
    Integer dueOffsetHours,
    Integer recurrenceFrequencyHours,
    Integer timeToCompleteMinutes,
    UUID associatedFolderId,
    UUID parentTaskId
) {}