// Organization.java
import java.util.List;
import java.util.UUID;

public class Organization {
    private UUID uuid;
    private String name;
    private UUID ownerId;
    private List<UUID> roleIds;

    public Organization() { this.uuid = UUID.randomUUID(); }
    // getters/setters...
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public List<UUID> getRoleIds() { return roleIds; }
    public void setRoleIds(List<UUID> roleIds) { this.roleIds = roleIds; }
}
