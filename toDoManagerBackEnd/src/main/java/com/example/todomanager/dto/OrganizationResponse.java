package com.example.todomanager.dto;
import java.util.UUID;

import com.example.todomanager.model.User;
public record OrganizationResponse(UUID id, String name, User owner) {}