package com.example.todomanager.dto;

import java.util.UUID;

public record TaskFolderResponse(UUID id, UUID userId, String title, String note) {}