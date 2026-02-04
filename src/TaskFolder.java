// TaskFolder.java
import java.util.UUID;

public class TaskFolder {
    private UUID id;
    private UUID userId;
    private String title;
    private String note;

    public TaskFolder() { this.id = UUID.randomUUID(); }
    // getters/setters...
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
