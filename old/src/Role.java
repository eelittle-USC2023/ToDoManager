// Role.java
import java.util.List;
import java.util.UUID;

public class Role {
    private UUID id;
    private String title;
    private UUID organizationId;
    private List<UUID> userIds; // holds user ids
    private String workHours; // e.g., "09:00-17:00" or a structured type if you prefer
    private double hoursPerWeek;

    public Role() { this.id = UUID.randomUUID(); }
    // getters/setters...
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public List<UUID> getUserIds() { return userIds; }
    public void setUserIds(List<UUID> userIds) { this.userIds = userIds; }
    public String getWorkHours() { return workHours; }
    public void setWorkHours(String workHours) { this.workHours = workHours; }
    public double getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(double hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }
}
