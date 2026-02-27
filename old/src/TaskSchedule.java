// TaskSchedule.java
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class TaskSchedule {
    private UUID uuid;
    private String title;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<java.time.LocalDate> dates; // list of valid dates; nullable or empty for open recurrence

    public TaskSchedule() { this.uuid = UUID.randomUUID(); }
    // getters/setters...
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public List<java.time.LocalDate> getDates() { return dates; }
    public void setDates(List<java.time.LocalDate> dates) { this.dates = dates; }
}
