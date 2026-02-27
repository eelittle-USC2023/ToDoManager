package com.example.todomanager.controller;

import com.example.todomanager.model.TaskFolder;
import com.example.todomanager.service.TaskFolderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
public class TaskFolderController {

    private final TaskFolderService folderService;

    public TaskFolderController(TaskFolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public TaskFolder createFolder(@RequestBody TaskFolder folder) {
        return folderService.create(folder);
    }

    @GetMapping("/{id}")
    public TaskFolder getById(@PathVariable UUID id) {
        return folderService.get(id);
    }

    @GetMapping("/user/{userId}")
    public List<TaskFolder> getByUser(@PathVariable UUID userId) {
        return folderService.getByUser(userId);
    }

    @PutMapping("/{id}")
    public TaskFolder edit(@PathVariable UUID id, @RequestBody TaskFolder folder) {
        folder.setId(id);
        return folderService.edit(folder);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        folderService.delete(id);
    }
}