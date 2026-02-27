package com.example.todomanager.controller;

import com.example.todomanager.model.Task;
import com.example.todomanager.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Task createTask(@RequestParam("creatingUserId") UUID creatingUserId,
                           @RequestBody Task taskRequest) {
        return taskService.create(taskRequest, creatingUserId);
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable UUID id) {
        return taskService.get(id);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable UUID id,
                        @RequestParam("actingUserId") UUID actingUserId,
                        @RequestBody Task taskUpdate) {
        taskUpdate.setId(id);
        return taskService.edit(taskUpdate, actingUserId);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable UUID id,
                           @RequestParam("actingUserId") UUID actingUserId) {
        taskService.delete(id, actingUserId);
    }

    @GetMapping("/user/{userId}")
    public List<Task> getByUser(@PathVariable UUID userId) {
        return taskService.getByUser(userId);
    }
}