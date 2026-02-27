-- testdata.sql
USE todomanager;

-- Clear tables (be careful in prod!)
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE role_users;
TRUNCATE TABLE tasks;
TRUNCATE TABLE task_folders;
TRUNCATE TABLE roles;
TRUNCATE TABLE organizations;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS=1;

-- 15 users
INSERT INTO users (id, username, password) VALUES
(UUID(), 'alice', 'password1'),
(UUID(), 'bob', 'password2'),
(UUID(), 'carol', 'password3'),
(UUID(), 'dave', 'password4'),
(UUID(), 'eve', 'password5'),
(UUID(), 'frank', 'password6'),
(UUID(), 'grace', 'password7'),
(UUID(), 'heidi', 'password8'),
(UUID(), 'ivan', 'password9'),
(UUID(), 'judy', 'password10'),
(UUID(), 'mallory', 'password11'),
(UUID(), 'oscar', 'password12'),
(UUID(), 'peggy', 'password13'),
(UUID(), 'trent', 'password14'),
(UUID(), 'victor', 'password15');

-- Grab some user ids into a temporary table for reference
DROP TEMPORARY TABLE IF EXISTS tmp_users;
CREATE TEMPORARY TABLE tmp_users AS SELECT id, username FROM users;

-- 3 organizations
INSERT INTO organizations (uuid, name, owner_id)
SELECT UUID(), CONCAT('Org ', FLOOR(RAND()*1000)), id FROM tmp_users ORDER BY RAND() LIMIT 3;

-- ============================================
-- Insert 7 independent roles
-- UUID stored as VARCHAR(36)
-- organization_id references organizations.id
-- ============================================

-- Role 1
SELECT uuid INTO @org1 FROM organizations ORDER BY RAND() LIMIT 1;
SET @role1 = UUID();

INSERT INTO roles
(id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
VALUES
(@role1, 'Role Alpha', @org1, NULL, '09:00-17:00', 'Mon-Fri', 40);


-- Role 2
SELECT uuid INTO @org2 FROM organizations ORDER BY RAND() LIMIT 1;
SET @role2 = UUID();

INSERT INTO roles
(id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
VALUES
(@role2, 'Role Beta', @org2, NULL, '09:00-17:00', 'Mon-Fri', 40);


-- Role 3
SELECT uuid INTO @org3 FROM organizations ORDER BY RAND() LIMIT 1;
SET @role3 = UUID();

INSERT INTO roles
(id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
VALUES
(@role3, 'Role Gamma', @org3, NULL, '09:00-17:00', 'Mon-Fri', 40);


-- Role 4
SELECT uuid INTO @org4 FROM organizations ORDER BY RAND() LIMIT 1;
SET @role4 = UUID();

INSERT INTO roles
(id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
VALUES
(@role4, 'Role Delta', @org4, NULL, '09:00-17:00', 'Mon-Fri', 40);


-- Role 5
SELECT uuid INTO @org5 FROM organizations ORDER BY RAND() LIMIT 1;
SET @role5 = UUID();

INSERT INTO roles
(id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
VALUES
(@role5, 'Role Epsilon', @org5, NULL, '09:00-17:00', 'Mon-Fri', 40);


-- Role 6
SELECT uuid INTO @org6 FROM organizations ORDER BY RAND() LIMIT 1;
SET @role6 = UUID();

INSERT INTO roles
(id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
VALUES
(@role6, 'Role Zeta', @org6, NULL, '09:00-17:00', 'Mon-Fri', 40);


-- Role 7
SELECT uuid INTO @org7 FROM organizations ORDER BY RAND() LIMIT 1;
SET @role7 = UUID();

INSERT INTO roles
(id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
VALUES
(@role7, 'Role Eta', @org7, NULL, '10:00-16:00', 'Mon-Fri', 20);

-- 15 folders (random user owners)
INSERT INTO task_folders (id, user_id, title, note)
SELECT UUID(), id, CONCAT('Folder for ', username, ' #', ROW_NUMBER() OVER (ORDER BY id)), CONCAT('Note for ', username)
FROM tmp_users
LIMIT 15;

-- 20 tasks
-- We pick random users and folders
INSERT INTO tasks (uuid, user_id, title, description, start_datetime, due_offset_hours, recurrence_frequency_hours, time_to_complete_minutes, folder_id, parent_task_id)
SELECT UUID(),
  (SELECT id FROM tmp_users ORDER BY RAND() LIMIT 1),
  CONCAT('Task ', LPAD(FLOOR(RAND()*10000),4,'0')),
  CONCAT('Auto-generated task description #', FLOOR(RAND()*10000)),
  DATE_ADD('2026-02-01 09:00:00', INTERVAL FLOOR(RAND()*10) DAY),
  FLOOR(RAND()*1440), -- 0-24h offset in minutes
  FLOOR(RAND()*48), -- recurrence hours 0..47 (0 may be non-recurring)
  FLOOR(RAND()*180)+10, -- time to complete hours
  (SELECT id FROM task_folders ORDER BY RAND() LIMIT 1),
  NULL -- parent will be set later for some tasks
FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20) t;

-- Pick a few tasks to be children of others
-- Pair 4 random child tasks to 4 random parent tasks (parents chosen from tasks NOT in children)

DROP TEMPORARY TABLE IF EXISTS tmp_children;
DROP TEMPORARY TABLE IF EXISTS tmp_parents;

CREATE TEMPORARY TABLE tmp_children (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid CHAR(36) NOT NULL
) ENGINE=MEMORY;

CREATE TEMPORARY TABLE tmp_parents (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid CHAR(36) NOT NULL
) ENGINE=MEMORY;

-- Insert 4 random distinct children
INSERT INTO tmp_children (uuid)
SELECT uuid FROM tasks
ORDER BY RAND()
LIMIT 4;

-- Insert 4 random parents that are NOT the selected children
INSERT INTO tmp_parents (uuid)
SELECT uuid FROM tasks
WHERE uuid NOT IN (SELECT uuid FROM tmp_children)
ORDER BY RAND()
LIMIT 4;

-- Now pair by id (1..4) and update tasks to set parent_task_id
UPDATE tasks t
JOIN tmp_children c ON t.uuid = c.uuid
JOIN tmp_parents p ON p.id = c.id
SET t.parent_task_id = p.uuid;

-- Optional verification
SELECT c.id AS pair_id, c.uuid AS child_uuid, p.uuid AS parent_uuid
FROM tmp_children c
JOIN tmp_parents p ON p.id = c.id;

DROP TEMPORARY TABLE IF EXISTS tmp_children;
DROP TEMPORARY TABLE IF EXISTS tmp_parents;

-- role_users mapping: assign random users to roles
INSERT INTO role_users (role_id, user_id)
SELECT r.id, u.id
FROM roles r
JOIN tmp_users u ON RAND() < 0.3 -- approx 30% mapping
LIMIT 30;

-- Clean up
DROP TEMPORARY TABLE IF EXISTS tmp_users;
