// DataAccess.java
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataAccess {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/todomanager?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "password";

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ----------------- Users CRUD -----------------
    public void createUser(User u) throws SQLException {
        String sql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getId().toString());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.executeUpdate();
        }
    }

    public User readUserById(UUID id) throws SQLException {
        String sql = "SELECT id, username, password FROM users WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(UUID.fromString(rs.getString("id")), rs.getString("username"), rs.getString("password"));
                }
            }
        }
        return null;
    }

    public List<User> readAllUsers() throws SQLException {
        String sql = "SELECT id, username, password FROM users";
        List<User> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new User(UUID.fromString(rs.getString("id")), rs.getString("username"), rs.getString("password")));
            }
        }
        return out;
    }

    public void updateUser(User u) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getId().toString());
            ps.executeUpdate();
        }
    }

    public void deleteUser(UUID id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        }
    }

    // ----------------- TaskFolder CRUD -----------------
    public void createFolder(TaskFolder f) throws SQLException {
        String sql = "INSERT INTO task_folders (id, user_id, title, note) VALUES (?, ?, ?, ?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f.getId().toString());
            ps.setString(2, f.getUserId().toString());
            ps.setString(3, f.getTitle());
            ps.setString(4, f.getNote());
            ps.executeUpdate();
        }
    }

    public TaskFolder readFolderById(UUID id) throws SQLException {
        String sql = "SELECT id, user_id, title, note FROM task_folders WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaskFolder f = new TaskFolder();
                    f.setId(UUID.fromString(rs.getString("id")));
                    f.setUserId(UUID.fromString(rs.getString("user_id")));
                    f.setTitle(rs.getString("title"));
                    f.setNote(rs.getString("note"));
                    return f;
                }
            }
        }
        return null;
    }

    public List<TaskFolder> readAllFolders() throws SQLException {
        String sql = "SELECT id, user_id, title, note FROM task_folders";
        List<TaskFolder> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TaskFolder f = new TaskFolder();
                f.setId(UUID.fromString(rs.getString("id")));
                f.setUserId(UUID.fromString(rs.getString("user_id")));
                f.setTitle(rs.getString("title"));
                f.setNote(rs.getString("note"));
                out.add(f);
            }
        }
        return out;
    }

    public void updateFolder(TaskFolder f) throws SQLException {
        String sql = "UPDATE task_folders SET title = ?, note = ? WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f.getTitle());
            ps.setString(2, f.getNote());
            ps.setString(3, f.getId().toString());
            ps.executeUpdate();
        }
    }

    public void deleteFolder(UUID id) throws SQLException {
        String sql = "DELETE FROM task_folders WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        }
    }

    // ----------------- TaskSchedule CRUD -----------------
    public void createSchedule(TaskSchedule s) throws SQLException {
        String sql = "INSERT INTO task_schedules (uuid, title, start_time, end_time, dates) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getUuid().toString());
            ps.setString(2, s.getTitle());
            ps.setTime(3, s.getStartTime() == null ? null : Time.valueOf(s.getStartTime()));
            ps.setTime(4, s.getEndTime() == null ? null : Time.valueOf(s.getEndTime()));
            if (s.getDates() == null) ps.setString(5, null);
            else {
                String json = s.getDates().stream().map(java.time.LocalDate::toString).collect(Collectors.joining("\",\"", "[\"", "\"]"));
                ps.setString(5, json);
            }
            ps.executeUpdate();
        }
    }

    public TaskSchedule readScheduleById(UUID id) throws SQLException {
        String sql = "SELECT uuid, title, start_time, end_time, dates FROM task_schedules WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaskSchedule s = new TaskSchedule();
                    s.setUuid(UUID.fromString(rs.getString("uuid")));
                    s.setTitle(rs.getString("title"));
                    Time st = rs.getTime("start_time");
                    Time et = rs.getTime("end_time");
                    s.setStartTime(st == null ? null : st.toLocalTime());
                    s.setEndTime(et == null ? null : et.toLocalTime());
                    String datesJson = rs.getString("dates");
                    if (datesJson != null) {
                        // crude parse: expecting ["2026-02-03","2026-02-04"]
                        datesJson = datesJson.replace("[","").replace("]","").replace("\"","");
                        List<java.time.LocalDate> list = new ArrayList<>();
                        if (!datesJson.trim().isEmpty()) {
                            for (String d : datesJson.split(",")) {
                                list.add(java.time.LocalDate.parse(d.trim()));
                            }
                        }
                        s.setDates(list);
                    }
                    return s;
                }
            }
        }
        return null;
    }

    public List<TaskSchedule> readAllSchedules() throws SQLException {
        String sql = "SELECT uuid FROM task_schedules";
        List<TaskSchedule> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(readScheduleById(UUID.fromString(rs.getString("uuid"))));
            }
        }
        return out;
    }

    public void updateSchedule(TaskSchedule s) throws SQLException {
        String sql = "UPDATE task_schedules SET title = ?, start_time = ?, end_time = ?, dates = ? WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getTitle());
            ps.setTime(2, s.getStartTime() == null ? null : Time.valueOf(s.getStartTime()));
            ps.setTime(3, s.getEndTime() == null ? null : Time.valueOf(s.getEndTime()));
            if (s.getDates() == null) ps.setString(4, null);
            else {
                String json = s.getDates().stream().map(java.time.LocalDate::toString).collect(Collectors.joining("\",\"", "[\"", "\"]"));
                ps.setString(4, json);
            }
            ps.setString(5, s.getUuid().toString());
            ps.executeUpdate();
        }
    }

    public void deleteSchedule(UUID id) throws SQLException {
        String sql = "DELETE FROM task_schedules WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        }
    }

    // ----------------- Tasks CRUD -----------------
    public void createTask(Task t) throws SQLException {
        String sql = "INSERT INTO tasks (uuid, user_id, title, description, start_datetime, due_offset_minutes, recurrence_frequency_hours, time_to_complete_minutes, folder_id, parent_task_id, schedule_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getUuid().toString());
            ps.setString(2, t.getUserId().toString());
            ps.setString(3, t.getTitle());
            ps.setString(4, t.getDescription());
            ps.setTimestamp(5, t.getStartDateTime() == null ? null : Timestamp.valueOf(t.getStartDateTime()));
            ps.setInt(6, (int)t.getDueOffsetMinutes());
            ps.setInt(7, (int)t.getRecurrenceFrequencyHours());
            ps.setInt(8, (int)t.getTimeToCompleteMinutes());
            ps.setString(9, t.getFolderId() == null ? null : t.getFolderId().toString());
            ps.setString(10, t.getParentTaskId() == null ? null : t.getParentTaskId().toString());
            ps.setString(11, t.getScheduleId() == null ? null : t.getScheduleId().toString());
            ps.executeUpdate();
        }
    }

    public Task readTaskById(UUID id) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Task t = new Task();
                    t.setUuid(UUID.fromString(rs.getString("uuid")));
                    t.setUserId(UUID.fromString(rs.getString("user_id")));
                    t.setTitle(rs.getString("title"));
                    t.setDescription(rs.getString("description"));
                    Timestamp ts = rs.getTimestamp("start_datetime");
                    t.setStartDateTime(ts == null ? null : ts.toLocalDateTime());
                    t.setDueOffsetMinutes(rs.getInt("due_offset_minutes"));
                    t.setRecurrenceFrequencyHours(rs.getInt("recurrence_frequency_hours"));
                    t.setTimeToCompleteMinutes(rs.getInt("time_to_complete_minutes"));
                    String folderId = rs.getString("folder_id");
                    t.setFolderId(folderId == null ? null : UUID.fromString(folderId));
                    String parent = rs.getString("parent_task_id");
                    t.setParentTaskId(parent == null ? null : UUID.fromString(parent));
                    String sched = rs.getString("schedule_id");
                    t.setScheduleId(sched == null ? null : UUID.fromString(sched));
                    return t;
                }
            }
        }
        return null;
    }

    public List<Task> readAllTasks() throws SQLException {
        String sql = "SELECT uuid FROM tasks";
        List<Task> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(readTaskById(UUID.fromString(rs.getString("uuid"))));
            }
        }
        return out;
    }

    public void updateTask(Task t) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, description = ?, start_datetime = ?, due_offset_minutes = ?, recurrence_frequency_hours = ?, time_to_complete_minutes = ?, folder_id = ?, parent_task_id = ?, schedule_id = ? WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setTimestamp(3, t.getStartDateTime() == null ? null : Timestamp.valueOf(t.getStartDateTime()));
            ps.setInt(4, (int)t.getDueOffsetMinutes());
            ps.setInt(5, (int)t.getRecurrenceFrequencyHours());
            ps.setInt(6, (int)t.getTimeToCompleteMinutes());
            ps.setString(7, t.getFolderId() == null ? null : t.getFolderId().toString());
            ps.setString(8, t.getParentTaskId() == null ? null : t.getParentTaskId().toString());
            ps.setString(9, t.getScheduleId() == null ? null : t.getScheduleId().toString());
            ps.setString(10, t.getUuid().toString());
            ps.executeUpdate();
        }
    }

    public void deleteTask(UUID id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        }
    }

    // ----------------- Roles CRUD -----------------
    public void createRole(Role r) throws SQLException {
        String sql = "INSERT INTO roles (id, title, organization_id, work_hours, hours_per_week) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getId().toString());
            ps.setString(2, r.getTitle());
            ps.setString(3, r.getOrganizationId() == null ? null : r.getOrganizationId().toString());
            ps.setString(4, r.getWorkHours());
            ps.setDouble(5, r.getHoursPerWeek());
            ps.executeUpdate();
        }
        // map users if any
        if (r.getUserIds() != null) {
            for (UUID uid : r.getUserIds()) {
                mapRoleToUser(r.getId(), uid);
            }
        }
    }

    public Role readRoleById(UUID id) throws SQLException {
        String sql = "SELECT id, title, organization_id, work_hours, hours_per_week FROM roles WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Role r = new Role();
                    r.setId(UUID.fromString(rs.getString("id")));
                    r.setTitle(rs.getString("title"));
                    String orgId = rs.getString("organization_id");
                    r.setOrganizationId(orgId == null ? null : UUID.fromString(orgId));
                    r.setWorkHours(rs.getString("work_hours"));
                    r.setHoursPerWeek(rs.getDouble("hours_per_week"));
                    // load users
                    r.setUserIds(getUsersForRole(r.getId()));
                    return r;
                }
            }
        }
        return null;
    }

    public List<Role> readAllRoles() throws SQLException {
        String sql = "SELECT id FROM roles";
        List<Role> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(readRoleById(UUID.fromString(rs.getString("id"))));
        }
        return out;
    }

    public void updateRole(Role r) throws SQLException {
        String sql = "UPDATE roles SET title = ?, organization_id = ?, work_hours = ?, hours_per_week = ? WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getTitle());
            ps.setString(3, r.getOrganizationId() == null ? null : r.getOrganizationId().toString());
            ps.setString(4, r.getWorkHours());
            ps.setDouble(5, r.getHoursPerWeek());
            ps.setString(6, r.getId().toString());
            ps.executeUpdate();
        }
        // If user mapping provided, clear & re-map
        if (r.getUserIds() != null) {
            clearUsersFromRole(r.getId());
            for (UUID uid : r.getUserIds()) mapRoleToUser(r.getId(), uid);
        }
    }

    public void deleteRole(UUID id) throws SQLException {
        // cascade via FK will remove role_users
        String sql = "DELETE FROM roles WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        }
    }

    private void mapRoleToUser(UUID roleId, UUID userId) throws SQLException {
        String sql = "INSERT IGNORE INTO role_users (role_id, user_id) VALUES (?, ?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleId.toString());
            ps.setString(2, userId.toString());
            ps.executeUpdate();
        }
    }

    private List<UUID> getUsersForRole(UUID roleId) throws SQLException {
        String sql = "SELECT user_id FROM role_users WHERE role_id = ?";
        List<UUID> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(UUID.fromString(rs.getString("user_id")));
            }
        }
        return out;
    }

    private void clearUsersFromRole(UUID roleId) throws SQLException {
        String sql = "DELETE FROM role_users WHERE role_id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleId.toString());
            ps.executeUpdate();
        }
    }

    // ----------------- Organizations CRUD -----------------
    public void createOrganization(Organization o) throws SQLException {
        String sql = "INSERT INTO organizations (uuid, name, owner_id) VALUES (?, ?, ?)";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, o.getUuid().toString());
            ps.setString(2, o.getName());
            ps.setString(3, o.getOwnerId() == null ? null : o.getOwnerId().toString());
            ps.executeUpdate();
        }
        // roles inserted separately, and roles reference organization_id
    }

    public Organization readOrganizationById(UUID id) throws SQLException {
        String sql = "SELECT uuid, name, owner_id FROM organizations WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Organization o = new Organization();
                    o.setUuid(UUID.fromString(rs.getString("uuid")));
                    o.setName(rs.getString("name"));
                    String owner = rs.getString("owner_id");
                    o.setOwnerId(owner == null ? null : UUID.fromString(owner));
                    // load roles for this org
                    o.setRoleIds(getRoleIdsForOrg(o.getUuid()));
                    return o;
                }
            }
        }
        return null;
    }

    public List<Organization> readAllOrganizations() throws SQLException {
        String sql = "SELECT uuid FROM organizations";
        List<Organization> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(readOrganizationById(UUID.fromString(rs.getString("uuid"))));
        }
        return out;
    }

    public void updateOrganization(Organization o) throws SQLException {
        String sql = "UPDATE organizations SET name = ?, owner_id = ? WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, o.getName());
            ps.setString(2, o.getOwnerId() == null ? null : o.getOwnerId().toString());
            ps.setString(3, o.getUuid().toString());
            ps.executeUpdate();
        }
    }

    public void deleteOrganization(UUID id) throws SQLException {
        String sql = "DELETE FROM organizations WHERE uuid = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        }
    }

    private List<UUID> getRoleIdsForOrg(UUID orgId) throws SQLException {
        String sql = "SELECT id FROM roles WHERE organization_id = ?";
        List<UUID> out = new ArrayList<>();
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, orgId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(UUID.fromString(rs.getString("id")));
            }
        }
        return out;
    }

    // ----------------- Convenience / cleanup methods -----------------
    // Add additional methods if needed
}
