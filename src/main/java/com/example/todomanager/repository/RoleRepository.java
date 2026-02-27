package com.example.todomanager.repository;

import com.example.todomanager.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    // find all roles that contain the user with id = userId
    List<Role> findByUsers_Id(UUID userId);
    List<Role> findByOrganization_Id(UUID organiztionId);
}