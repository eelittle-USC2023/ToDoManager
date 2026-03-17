package com.example.todomanager.dto;

import java.util.UUID;

public record CreateTaskFolderRequest(UUID userId, String title, String note) {}