// User.java
import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String password; // store hashed in production

    public User() { this.id = UUID.randomUUID(); }
    public User(UUID id, String username, String password) { this.id = id; this.username = username; this.password = password; }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
