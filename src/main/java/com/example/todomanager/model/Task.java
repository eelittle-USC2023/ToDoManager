package com.example.todomanager.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @Column(name = "uuid", columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "char(36)")
    private User user; // assigned user

    private String title;

    @Column(length = 4000)
    private String description;

    @Column(name = "start_datetime")
    private OffsetDateTime startDateTime;

    // due represented as offset from start in hours
    private int dueOffsetHours;

    // recurrence frequency in hours (0 = no recurrence)
    private int recurrenceFrequencyHours;

    // time to complete in minutes
    private int timeToCompleteMinutes;

    @ManyToOne
    @JoinColumn(name = "folder_id", columnDefinition = "char(36)")
    private TaskFolder folder;

    @ManyToOne
    @JoinColumn(name = "parent_task_id", columnDefinition = "char(36)")
    private Task parentTask;

    public Task() {}

    public Task(UUID id, User user, String title) {
        this.id = id;
        this.user = user;
        this.title = title;
    }

    // getters/setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OffsetDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(OffsetDateTime startDateTime) { this.startDateTime = startDateTime; }

    public int getDueOffsetHours() { return dueOffsetHours; }
    public void setDueOffsetHours(int dueOffsetHours) { this.dueOffsetHours = dueOffsetHours; }

    public int getRecurrenceFrequencyHours() { return recurrenceFrequencyHours; }
    public void setRecurrenceFrequencyHours(int recurrenceFrequencyHours) { this.recurrenceFrequencyHours = recurrenceFrequencyHours; }

    public int getTimeToCompleteMinutes() { return timeToCompleteMinutes; }
    public void setTimeToCompleteMinutes(int timeToCompleteMinutes) { this.timeToCompleteMinutes = timeToCompleteMinutes; }

    public TaskFolder getFolder() { return folder; }
    public void setFolder(TaskFolder folder) { this.folder = folder; }

    public Task getParentTask() { return parentTask; }
    public void setParentTask(Task parentTask) { this.parentTask = parentTask; }

    public OffsetDateTime getDueDateTime() {
        if (startDateTime == null) return null;
        return startDateTime.plusHours(dueOffsetHours);
    }
}