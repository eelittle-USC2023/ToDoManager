-- schema.sql
CREATE DATABASE IF NOT EXISTS todomanager;
USE todomanager;

-- Users
CREATE TABLE users (
  id CHAR(36) PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Organizations
CREATE TABLE organizations (
  uuid CHAR(36) PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  owner_id CHAR(36),
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Roles
CREATE TABLE roles (
  id CHAR(36) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  organization_id CHAR(36),
  work_hours VARCHAR(100),
  hours_per_week DECIMAL(6,2),
  supervisor_role_id VARCHAR(36),
  work_days VARCHAR(15),
  FOREIGN KEY (organization_id) REFERENCES organizations(uuid) ON DELETE CASCADE
);

-- Roles <-> Users (many-to-many)
CREATE TABLE role_users (
  role_id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  PRIMARY KEY (role_id, user_id),
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Task folders
CREATE TABLE task_folders (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  title VARCHAR(200),
  note TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tasks
CREATE TABLE tasks (
  uuid CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  start_datetime DATETIME,
  due_offset_hours INT DEFAULT 0, -- due date as offset in hours from start
  recurrence_frequency_hours INT DEFAULT 0,
  time_to_complete_minutes INT DEFAULT 0,
  folder_id CHAR(36),
  parent_task_id CHAR(36),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (folder_id) REFERENCES task_folders(id) ON DELETE SET NULL,
  FOREIGN KEY (parent_task_id) REFERENCES tasks(uuid) ON DELETE SET NULL
);
