package com.example.todomanager.repository;

import com.example.todomanager.model.TaskFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface TaskFolderRepository extends JpaRepository<TaskFolder, UUID> {
    Optional<TaskFolder> findByUserIdAndTitle(UUID userId, String title);
    List<TaskFolder> findByUser_Id(UUID userId);
}