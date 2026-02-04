// Task.java
import java.time.LocalDateTime;
import java.util.UUID;

public class Task {
    private UUID uuid;
    private UUID userId;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private long dueOffsetMinutes; // due as offset from start in minutes
    private long recurrenceFrequencyHours; // 0 = non-recurring
    private long timeToCompleteMinutes;
    private UUID folderId; // nullable
    private UUID parentTaskId; // nullable
    private UUID scheduleId; // nullable

    public Task() { this.uuid = UUID.randomUUID(); }

    // getters & setters omitted for brevity in this listing — implement for all fields
    // ... (generate typical getters/setters)
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime s) { this.startDateTime = s; }
    public long getDueOffsetMinutes() { return dueOffsetMinutes; }
    public void setDueOffsetMinutes(long dueOffsetMinutes) { this.dueOffsetMinutes = dueOffsetMinutes; }
    public long getRecurrenceFrequencyHours() { return recurrenceFrequencyHours; }
    public void setRecurrenceFrequencyHours(long recurrenceFrequencyHours) { this.recurrenceFrequencyHours = recurrenceFrequencyHours; }
    public long getTimeToCompleteMinutes() { return timeToCompleteMinutes; }
    public void setTimeToCompleteMinutes(long t) { this.timeToCompleteMinutes = t; }
    public UUID getFolderId() { return folderId; }
    public void setFolderId(UUID folderId) { this.folderId = folderId; }
    public UUID getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(UUID parentTaskId) { this.parentTaskId = parentTaskId; }
    public UUID getScheduleId() { return scheduleId; }
    public void setScheduleId(UUID scheduleId) { this.scheduleId = scheduleId; }
}
