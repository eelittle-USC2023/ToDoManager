package com.example.todomanager.controller;

import com.example.todomanager.dto.*;
import com.example.todomanager.model.TaskFolder;
import com.example.todomanager.service.TaskFolderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/folders")
public class TaskFolderController {

    private final TaskFolderService folderService;

    public TaskFolderController(TaskFolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public ResponseEntity<TaskFolderResponse> createFolder(@RequestBody CreateTaskFolderRequest req) {
        TaskFolder folder = com.example.todomanager.dto.DtoMapper.fromCreateTaskFolderRequest(req);
        TaskFolder saved = folderService.create(folder);
        TaskFolderResponse resp = com.example.todomanager.dto.DtoMapper.toTaskFolderResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskFolderResponse> getById(@PathVariable UUID id) {
        TaskFolder folder = folderService.get(id);
        return ResponseEntity.ok(com.example.todomanager.dto.DtoMapper.toTaskFolderResponse(folder));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskFolderResponse>> getByUser(@PathVariable UUID userId) {
        List<TaskFolder> folders = folderService.getByUser(userId);
        var resp = folders.stream().map(com.example.todomanager.dto.DtoMapper::toTaskFolderResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskFolderResponse> edit(@PathVariable UUID id, @RequestBody UpdateTaskFolderRequest req) {
        TaskFolder folder = new TaskFolder();
        folder.setId(id);
        com.example.todomanager.dto.DtoMapper.applyUpdateTaskFolderRequest(folder, req);
        TaskFolder updated = folderService.edit(folder);
        return ResponseEntity.ok(com.example.todomanager.dto.DtoMapper.toTaskFolderResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        folderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}