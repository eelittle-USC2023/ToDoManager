// Main.java
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static void main(String[] args) {
        DataAccess dao = new DataAccess();

        try {
            // Users
            System.out.println("=== USERS ===");
            User u = new User(UUID.randomUUID(), "tester", "secret");
            dao.createUser(u);
            System.out.println("Created user: " + u.getUsername());

            List<User> users = dao.readAllUsers();
            System.out.println("Total users: " + users.size());

            User got = dao.readUserById(u.getId());
            System.out.println("Read user by id: " + (got != null ? got.getUsername() : "null"));

            u.setPassword("newsecret");
            dao.updateUser(u);
            System.out.println("Updated user password");

            dao.deleteUser(u.getId());
            System.out.println("Deleted user.");

            // Folders
            System.out.println("\n=== FOLDERS ===");
            // pick an existing user id for folder owner
            List<User> existing = dao.readAllUsers();
            if (!existing.isEmpty()) {
                User owner = existing.get(0);
                TaskFolder f = new TaskFolder();
                f.setUserId(owner.getId());
                f.setTitle("CLI Folder");
                f.setNote("Created by CLI tester");
                dao.createFolder(f);
                System.out.println("Created folder: " + f.getTitle());
                List<TaskFolder> folders = dao.readAllFolders();
                System.out.println("Folders count: " + folders.size());
                dao.deleteFolder(f.getId());
                System.out.println("Deleted folder.");
            } else {
                System.out.println("No users exist to create folder owner.");
            }

            // Schedules
            System.out.println("\n=== SCHEDULES ===");
            TaskSchedule s = new TaskSchedule();
            s.setTitle("CLI Schedule");
            s.setStartTime(LocalTime.of(9,0));
            s.setEndTime(LocalTime.of(10,0));
            s.setDates(Arrays.asList(LocalDate.now(), LocalDate.now().plusDays(1)));
            dao.createSchedule(s);
            System.out.println("Created schedule: " + s.getTitle());
            List<TaskSchedule> schedules = dao.readAllSchedules();
            System.out.println("Schedules: " + schedules.size());
            dao.deleteSchedule(s.getUuid());
            System.out.println("Deleted schedule.");

            // Tasks
            System.out.println("\n=== TASKS ===");
            // create a user to own a task
            User taskOwner = new User(UUID.randomUUID(), "taskowner", "pw");
            dao.createUser(taskOwner);
            Task t = new Task();
            t.setUserId(taskOwner.getId());
            t.setTitle("CLI Task");
            t.setDescription("Task created by CLI");
            t.setStartDateTime(LocalDateTime.now().plusDays(1));
            t.setDueOffsetMinutes(60);
            t.setRecurrenceFrequencyHours(0);
            t.setTimeToCompleteMinutes(30);
            dao.createTask(t);
            System.out.println("Created task: " + t.getTitle());

            List<Task> tasks = dao.readAllTasks();
            System.out.println("Total tasks: " + tasks.size());

            Task readTask = dao.readTaskById(t.getUuid());
            System.out.println("Read task title: " + (readTask != null ? readTask.getTitle() : "null"));

            // update
            t.setTitle("CLI Task (updated)");
            dao.updateTask(t);
            System.out.println("Updated task.");

            // delete
            dao.deleteTask(t.getUuid());
            System.out.println("Deleted task.");

            // Roles & Organizations
            System.out.println("\n=== ORGS & ROLES ===");
            Organization org = new Organization();
            org.setUuid(UUID.randomUUID());
            org.setName("CLI Org");
            // pick an owner if available
            existing = dao.readAllUsers();
            if (!existing.isEmpty()) org.setOwnerId(existing.get(0).getId());
            dao.createOrganization(org);
            System.out.println("Created org: " + org.getName());

            Role role = new Role();
            role.setId(UUID.randomUUID());
            role.setTitle("CLI Role");
            role.setOrganizationId(org.getUuid());
            role.setWorkHours("09:00-17:00");
            role.setHoursPerWeek(40);
            // assign some users to role
            role.setUserIds(existing.stream().limit(2).map(User::getId).collect(java.util.stream.Collectors.toList()));
            dao.createRole(role);
            System.out.println("Created role: " + role.getTitle());

            Role readRole = dao.readRoleById(role.getId());
            System.out.println("Role has users: " + (readRole.getUserIds() == null ? 0 : readRole.getUserIds().size()));

            // cleanup sample created items
            dao.deleteRole(role.getId());
            dao.deleteOrganization(org.getUuid());
            dao.deleteUser(taskOwner.getId()); // created earlier

            System.out.println("\nAll CLI tests finished.");

            listAllUsers(dao);
            listAllFolders(dao);
            listAllSchedules(dao);
            listAllTasks(dao);
            listAllRoles(dao);
            listAllOrganizations(dao);
        } catch (Exception ex) {
            System.err.println("Error listing data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void listAllUsers(DataAccess dao) throws Exception {
        System.out.println("\n=== USERS ===");
        List<User> users = dao.readAllUsers();
        if (users.isEmpty()) {
            System.out.println("(no users found)");
            return;
        }
        System.out.printf("%-36s  %-15s  %-10s%n", "ID", "Username", "Password");
        System.out.println("-".repeat(70));
        for (User u : users) {
            System.out.printf("%-36s  %-15s  %-10s%n",
                    u.getId().toString(),
                    safe(u.getUsername()),
                    safe(u.getPassword()));
        }
    }

    private static void listAllFolders(DataAccess dao) throws Exception {
        System.out.println("\n=== TASK FOLDERS ===");
        List<TaskFolder> folders = dao.readAllFolders();
        if (folders.isEmpty()) {
            System.out.println("(no folders found)");
            return;
        }
        System.out.printf("%-36s  %-36s  %-25s  %s%n", "Folder ID", "User ID", "Title", "Note");
        System.out.println("-".repeat(120));
        for (TaskFolder f : folders) {
            System.out.printf("%-36s  %-36s  %-25s  %s%n",
                    f.getId().toString(),
                    f.getUserId() == null ? "" : f.getUserId().toString(),
                    truncate(f.getTitle(), 25),
                    safe(f.getNote()));
        }
    }

    private static void listAllSchedules(DataAccess dao) throws Exception {
        System.out.println("\n=== TASK SCHEDULES ===");
        List<TaskSchedule> schedules = dao.readAllSchedules();
        if (schedules.isEmpty()) {
            System.out.println("(no schedules found)");
            return;
        }
        System.out.printf("%-36s  %-20s  %-8s  %-8s  %s%n", "Schedule ID", "Title", "Start", "End", "Dates");
        System.out.println("-".repeat(120));
        for (TaskSchedule s : schedules) {
            String start = s.getStartTime() == null ? "" : s.getStartTime().toString();
            String end = s.getEndTime() == null ? "" : s.getEndTime().toString();
            String dates = (s.getDates() == null || s.getDates().isEmpty()) ? "[]" :
                    s.getDates().stream().map(Object::toString).collect(Collectors.joining(","));
            System.out.printf("%-36s  %-20s  %-8s  %-8s  %s%n",
                    s.getUuid().toString(),
                    truncate(s.getTitle(), 20),
                    start, end, dates);
        }
    }

    private static void listAllTasks(DataAccess dao) throws Exception {
        System.out.println("\n=== TASKS ===");
        List<Task> tasks = dao.readAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("(no tasks found)");
            return;
        }
        System.out.printf("%-36s  %-36s  %-20s  %-16s  %-6s  %-6s  %-6s  %-36s  %-36s  %-36s%n",
                "Task ID", "User ID", "Title", "Start", "DueM", "RecH", "TTC", "Folder ID", "Parent ID", "Schedule ID");
        System.out.println("-".repeat(220));
        for (Task t : tasks) {
            String start = t.getStartDateTime() == null ? "" : dtf.format(t.getStartDateTime());
            System.out.printf("%-36s  %-36s  %-20s  %-16s  %-6d  %-6d  %-6d  %-36s  %-36s  %-36s%n",
                    t.getUuid().toString(),
                    t.getUserId() == null ? "" : t.getUserId().toString(),
                    truncate(t.getTitle(), 20),
                    start,
                    t.getDueOffsetMinutes(),
                    t.getRecurrenceFrequencyHours(),
                    t.getTimeToCompleteMinutes(),
                    t.getFolderId() == null ? "" : t.getFolderId().toString(),
                    t.getParentTaskId() == null ? "" : t.getParentTaskId().toString(),
                    t.getScheduleId() == null ? "" : t.getScheduleId().toString()
            );
        }
    }

    private static void listAllRoles(DataAccess dao) throws Exception {
        System.out.println("\n=== ROLES ===");
        List<Role> roles = dao.readAllRoles();
        if (roles.isEmpty()) {
            System.out.println("(no roles found)");
            return;
        }
        System.out.printf("%-36s  %-20s  %-36s  %-12s  %-8s  %s%n",
                "Role ID", "Title", "Organization ID", "WorkHours", "Hrs/Wk", "User IDs");
        System.out.println("-".repeat(160));
        for (Role r : roles) {
            String userIds = (r.getUserIds() == null || r.getUserIds().isEmpty()) ? "[]" :
                    r.getUserIds().stream().map(UUID::toString).collect(Collectors.joining(","));
            System.out.printf("%-36s  %-20s  %-36s  %-12s  %-8.2f  %s%n",
                    r.getId().toString(),
                    truncate(r.getTitle(), 20),
                    r.getOrganizationId() == null ? "" : r.getOrganizationId().toString(),
                    safe(r.getWorkHours()),
                    r.getHoursPerWeek(),
                    userIds);
        }
    }

    private static void listAllOrganizations(DataAccess dao) throws Exception {
        System.out.println("\n=== ORGANIZATIONS ===");
        List<Organization> orgs = dao.readAllOrganizations();
        if (orgs.isEmpty()) {
            System.out.println("(no organizations found)");
            return;
        }
        System.out.printf("%-36s  %-25s  %-36s  %s%n", "Org ID", "Name", "Owner ID", "Role IDs");
        System.out.println("-".repeat(140));
        for (Organization o : orgs) {
            String roleIds = (o.getRoleIds() == null || o.getRoleIds().isEmpty()) ? "[]" :
                    o.getRoleIds().stream().map(UUID::toString).collect(Collectors.joining(","));
            System.out.printf("%-36s  %-25s  %-36s  %s%n",
                    o.getUuid().toString(),
                    truncate(o.getName(), 25),
                    o.getOwnerId() == null ? "" : o.getOwnerId().toString(),
                    roleIds);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }
}

