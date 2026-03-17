package com.example.todomanager.dto;
import java.util.UUID;

public record CreateOrganizationRequest(String name, UUID ownerId) {}
