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

-- safe reload of tmp tables
DROP TEMPORARY TABLE IF EXISTS tmp_users;
DROP TEMPORARY TABLE IF EXISTS new_orgs;
DROP TEMPORARY TABLE IF EXISTS driver;
DROP TEMPORARY TABLE IF EXISTS role_titles;
DROP TEMPORARY TABLE IF EXISTS owner_roles;

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
CREATE TEMPORARY TABLE tmp_users AS SELECT id, username FROM users;

-- Create 3 new organizations owned by 3 random users and store owner-role ids
CREATE TEMPORARY TABLE new_orgs AS
SELECT
  UUID() AS uuid,
  CONCAT('Org ', FLOOR(RAND()*1000)) AS name,
  u.id AS owner_id,
  UUID() AS owner_role_id
FROM tmp_users u
ORDER BY RAND()
LIMIT 3;

-- Insert those organizations into the real table
INSERT INTO organizations (uuid, name, owner_id)
SELECT uuid, name, owner_id FROM new_orgs;

-- Insert Owner roles using the pre-generated owner_role_id so we can reference them later
INSERT INTO roles (id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
SELECT owner_role_id AS id,
       'Owner' AS title,
       uuid AS organization_id,
       NULL AS supervisor_role_id,
       '09:00-17:00' AS work_hours,
       'Mon-Fri' AS work_days,
       40 AS hours_per_week
FROM new_orgs;

-- Getting owner role IDs for later
CREATE TEMPORARY TABLE owner_roles AS
SELECT owner_role_id FROM new_orgs;

-- Prepare 4 role definitions (titles + settings)
CREATE TEMPORARY TABLE role_titles AS
SELECT 1 AS rn, 'Role Alpha' AS title, '09:00-17:00' AS work_hours, 'Mon-Fri' AS work_days, 40.00 AS hours_per_week
UNION ALL
SELECT 2, 'Role Beta',  '09:00-17:00', 'Mon-Fri', 40.00
UNION ALL
SELECT 3, 'Role Gamma', '09:00-17:00', 'Mon-Fri', 40.00
UNION ALL
SELECT 4, 'Role Delta', '10:00-16:00', 'Mon-Fri', 20.00;

-- small driver table to produce exactly 4 rows (one insert per title)
CREATE TEMPORARY TABLE driver AS
SELECT 1 AS rn UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4;

-- Insert the 4 additional roles.
-- For each driver row we pick one of the three new_orgs at random (ORDER BY RAND() LIMIT 1 evaluated per driver row),
-- and set that role's supervisor_role_id to the owner_role_id for the picked organization.
INSERT INTO roles (id, title, organization_id, supervisor_role_id, work_hours, work_days, hours_per_week)
SELECT
  UUID() AS id,
  rt.title,
  pick.uuid AS organization_id,
  pick.owner_role_id AS supervisor_role_id,
  rt.work_hours,
  rt.work_days,
  rt.hours_per_week
FROM driver d
JOIN role_titles rt ON rt.rn = d.rn
CROSS JOIN (
  -- pick one of the new_orgs at random for this driver row
  SELECT uuid, owner_role_id
  FROM new_orgs
  ORDER BY RAND()
  LIMIT 1
) AS pick;

-- Map random users into the roles we just created.
-- We only map users to roles that belong to the organizations we just created
-- (i.e., roles whose organization_id is in new_orgs).
-- The sample condition RAND() < 0.3 approximates a 30% chance per pairing; LIMIT 30 keeps volume bounded.
-- Map random users into NON-owner roles only
INSERT INTO role_users (role_id, user_id)
SELECT r.id, u.id
FROM roles r
JOIN tmp_users u
  ON RAND() < 0.3
LEFT JOIN owner_roles o
  ON r.id = o.owner_role_id
WHERE r.organization_id IN (SELECT uuid FROM new_orgs)
  AND o.owner_role_id IS NULL
LIMIT 30;

-- Insert role-user mappings for Owner roles into many-to-many table
Insert into role_users (role_id, user_id)
SELECT owner_role_id, owner_id From new_orgs;

-- 15 folders (random user owners)
INSERT INTO task_folders (id, user_id, title, note)
SELECT UUID(), id, CONCAT('Folder for ', username, ' #', ROW_NUMBER() OVER (ORDER BY id)), CONCAT('Note for ', username)
FROM tmp_users
LIMIT 15;

-- 20 tasks
-- We pick random users and folders
-- build a numbered list of tmp_users so we can pick by row number
DROP TEMPORARY TABLE IF EXISTS numbered_users;
CREATE TEMPORARY TABLE numbered_users AS
SELECT id,
       ROW_NUMBER() OVER (ORDER BY id) AS rn
FROM tmp_users;

-- total users for offset math
SET @total_users = (SELECT COUNT(*) FROM numbered_users);

-- guard: if there are zero users, stop here (avoid divide-by-zero / invalid picks)
SELECT @total_users AS total_users;
-- If total_users = 0, insert nothing or populate tmp_users first.

-- Insert N tasks; change UNION block to change how many rows you want
INSERT INTO tasks (
  uuid,
  user_id,
  title,
  description,
  start_datetime,
  due_offset_hours,
  recurrence_frequency_hours,
  time_to_complete_minutes,
  folder_id,
  parent_task_id
)
SELECT
  UUID() AS uuid,
  nu.id AS user_id,
  CONCAT('Task ', LPAD(FLOOR(RAND()*10000),4,'0')) AS title,
  CONCAT('Auto-generated task description #', FLOOR(RAND()*10000)) AS description,
  DATE_ADD('2026-02-01 09:00:00', INTERVAL FLOOR(RAND()*10) DAY) AS start_datetime,
  FLOOR(RAND()*1440) AS due_offset_hours,
  FLOOR(RAND()*48) AS recurrence_frequency_hours,
  FLOOR(RAND()*180) + 10 AS time_to_complete_minutes,
  -- pick a random folder for that user; returns NULL if user has no folders
  ( SELECT tf.id
    FROM task_folders tf
    WHERE tf.user_id = nu.id
    ORDER BY RAND()
    LIMIT 1
  ) AS folder_id,
  NULL AS parent_task_id
FROM
  (
    -- drive table: 20 rows (adjust as needed)
    SELECT 1 AS rn UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
  ) AS t
  /* join to exactly one numbered user chosen by a per-row random expression */
  JOIN numbered_users nu
    ON nu.rn = FLOOR(RAND() * @total_users) + 1;

-- I have dropped the parent-child task relationship from the SQL test data.

-- cleanup temporaries
DROP TEMPORARY TABLE IF EXISTS driver;
DROP TEMPORARY TABLE IF EXISTS role_titles;
DROP TEMPORARY TABLE IF EXISTS new_orgs;
DROP TEMPORARY TABLE IF EXISTS tmp_users;
DROP TEMPORARY TABLE IF EXISTS owner_roles;