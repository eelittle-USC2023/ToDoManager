package com.example.todomanager.controller;

import com.example.todomanager.dto.*;
import com.example.todomanager.model.Task;
import com.example.todomanager.service.TaskService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Create task. creatingUserId is required as request parameter (who creates the task).
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestParam("creatingUserId") UUID creatingUserId,
                                                   @RequestBody CreateTaskRequest req) {
        Task t = com.example.todomanager.dto.DtoMapper.fromCreateTaskRequest(req);
        Task saved = taskService.create(t, creatingUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(com.example.todomanager.dto.DtoMapper.toTaskResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID id) {
        Task t = taskService.get(id);
        return ResponseEntity.ok(com.example.todomanager.dto.DtoMapper.toTaskResponse(t));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID id,
                                                   @RequestParam("actingUserId") UUID actingUserId,
                                                   @RequestBody UpdateTaskRequest req) {
        Task update = new Task();
        update.setId(id);
        com.example.todomanager.dto.DtoMapper.applyUpdateTaskRequest(update, req);
        Task saved = taskService.edit(update, actingUserId);
        return ResponseEntity.ok(com.example.todomanager.dto.DtoMapper.toTaskResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id,
                                           @RequestParam("actingUserId") UUID actingUserId) {
        taskService.delete(id, actingUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponse>> getByUser(@PathVariable UUID userId) {
        List<Task> tasks = taskService.getByUser(userId);
        var resp = tasks.stream().map(com.example.todomanager.dto.DtoMapper::toTaskResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }
}