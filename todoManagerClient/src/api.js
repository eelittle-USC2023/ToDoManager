// src/api.js
const BASE = 'http://localhost:8080/api';

function authHeader() {
  // No token-based auth for now — return empty headers object.
  return {};
}

function buildUrl(path, qs) {
  if (!qs) return `${BASE}${path}`;
  const usp = new URLSearchParams();
  for (const [k, v] of Object.entries(qs)) {
    if (v === undefined || v === null) continue;
    usp.append(k, String(v));
  }
  return `${BASE}${path}?${usp.toString()}`;
}

async function handleResponse(res) {
  // handle no-content and text/json consistently
  if (res.status === 204) return null;

  const ct = res.headers.get('content-type') || '';
  const isJson = ct.includes('application/json');
  const payload = isJson ? await res.json().catch(() => null) : await res.text().catch(() => null);

  if (!res.ok) {
    const message = (payload && (payload.message || payload.error)) || payload || res.statusText;
    const err = new Error(message || 'Request failed');
    err.status = res.status;
    err.payload = payload;
    throw err;
  }
  return payload;
}

export default {
  // -----------------------
  // Users
  // -----------------------
  createUser: (username, password) =>
    fetch(`${BASE}/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify({ username, password })
    }).then(handleResponse),

  // login: backend returns a short object { id, username } (no token).
  // Normalize to { user: {...} } so authContext can always expect .user
  login: async ({ username, password }) => {
    const payload = await fetch(`${BASE}/users/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    }).then(handleResponse);

    // Normalize: if payload already contains a `user` object, use it.
    if (payload && payload.user) return { user: payload.user };
    // If payload looks like { id, username }, wrap it:
    if (payload && (payload.id || payload.username)) return { user: { id: payload.id, username: payload.username } };
    // Fallback: return what we got (caller should handle)
    return { user: payload };
  },

  getUser: (id) =>
    fetch(`${BASE}/users/${id}`, { headers: { ...authHeader() } }).then(handleResponse),

  editUser: (id, { username, password }) =>
    fetch(`${BASE}/users/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify({ username, password })
    }).then(handleResponse),

  deleteUser: (id) =>
    fetch(`${BASE}/users/${id}`, { method: 'DELETE', headers: { ...authHeader() } }).then(handleResponse),

  getAllOrganizationsForUser: (userId) =>
    fetch(`${BASE}/users/${userId}/organizations`, { headers: { ...authHeader() } }).then(handleResponse),

  // -----------------------
  // Task Folders
  // -----------------------
  createFolder: ({ userId, title, note }) =>
    fetch(`${BASE}/folders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify({ userId, title, note })
    }).then(handleResponse),

  getFolder: (id) =>
    fetch(`${BASE}/folders/${id}`, { headers: { ...authHeader() } }).then(handleResponse),

  getFoldersByUser: (userId) =>
    fetch(`${BASE}/folders/user/${userId}`, { headers: { ...authHeader() } }).then(handleResponse),

  // editFolder now only sends title and note (strings).
  // Empty string "" means "clear" for strings per your API design.
  editFolder: (id, { title, note }) =>
    fetch(`${BASE}/folders/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify({ title, note })
    }).then(handleResponse),

  deleteFolder: (id) =>
    fetch(`${BASE}/folders/${id}`, { method: 'DELETE', headers: { ...authHeader() } }).then(handleResponse),

  // -----------------------
  // Tasks
  // -----------------------
  createTask: (creatingUserId, taskDto) =>
    fetch(buildUrl('/tasks', { creatingUserId }), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(taskDto)
    }).then(handleResponse),

  getTask: (id) =>
    fetch(`${BASE}/tasks/${id}`, { headers: { ...authHeader() } }).then(handleResponse),

  // editTask: taskDto should follow UpdateTaskRequest shape:
  // { title, description, startDateTime, dueOffsetHours, recurrenceFrequencyHours,
  //   timeToCompleteMinutes, associatedFolderId, clearAssociatedFolder, parentTaskId, clearParentTask }
  editTask: (id, actingUserId, taskDto) =>
    fetch(buildUrl(`/tasks/${id}`, { actingUserId }), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(taskDto)
    }).then(handleResponse),

  deleteTask: (id, actingUserId) =>
    fetch(buildUrl(`/tasks/${id}`, { actingUserId }), {
      method: 'DELETE',
      headers: { ...authHeader() }
    }).then(handleResponse),

  getTasksByUser: (userId) =>
    fetch(`${BASE}/tasks/user/${userId}`, { headers: { ...authHeader() } }).then(handleResponse),

  // -----------------------
  // Organizations
  // -----------------------
  createOrganization: ({ ownerId, name }) =>
    fetch(`${BASE}/organizations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify({ name, ownerId })
    }).then(handleResponse),

  getOrganization: (id, actingUserId) =>
    fetch(buildUrl(`/organizations/${id}`, { actingUserId }), { headers: { ...authHeader() } }).then(handleResponse),

  // editOrganization only sends the name now (strings: "" clears).
  editOrganization: (id, actingUserId, { name }) =>
    fetch(buildUrl(`/organizations/${id}`, { actingUserId }), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify({ name })
    }).then(handleResponse),

  deleteOrganization: (id, actingUserId) =>
    fetch(buildUrl(`/organizations/${id}`, { actingUserId }), {
      method: 'DELETE',
      headers: { ...authHeader() }
    }).then(handleResponse),

  getAllRolesForOrg: (orgId, actingUserId) =>
    fetch(buildUrl(`/organizations/${orgId}/roles`, { actingUserId }), { headers: { ...authHeader() } }).then(handleResponse),

  // -----------------------
  // Roles
  // -----------------------
  createRole: (actingUserId, roleDto) =>
    fetch(buildUrl('/roles', { actingUserId }), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(roleDto)
    }).then(handleResponse),

  // editRole: roleDto should follow UpdateRoleRequest shape:
  // { title, supervisorRoleId, clearSupervisorRole, userIds, clearUsers, workHours, workDays, hoursPerWeek }
  editRole: (id, actingUserId, roleDto) =>
    fetch(buildUrl(`/roles/${id}`, { actingUserId }), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(roleDto)
    }).then(handleResponse),

  getRole: (id, actingUserId) =>
    fetch(buildUrl(`/roles/${id}`, { actingUserId }), { headers: { ...authHeader() } }).then(handleResponse),

  deleteRole: (id, actingUserId) =>
    fetch(buildUrl(`/roles/${id}`, { actingUserId }), {
      method: 'DELETE',
      headers: { ...authHeader() }
    }).then(handleResponse),

  getAllUsersForRole: (roleId, actingUserId) =>
    fetch(buildUrl(`/roles/${roleId}/users`, { actingUserId }), { headers: { ...authHeader() } }).then(handleResponse)
};