import React, { useEffect, useState } from 'react';
import api from '../api';
import { useAuth } from '../authContext';
import RoleDetail from './RoleDetail';

/**
 * OrgDetail
 * - shows org name + owner
 * - lists the roles (only the ones the user is a member of, as before)
 * - provides a modal to create/edit roles (owner only)
 */
export default function OrgDetail({ org, refresh }) {
  const { user } = useAuth();
  const [rolesForUser, setRolesForUser] = useState([]); // roles the current user belongs to
  const [rolesAll, setRolesAll] = useState([]); // all roles for supervisor dropdown
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState(null);

  // modal state
  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [editingRole, setEditingRole] = useState(null); // null => create, otherwise role object
  const [modalSaving, setModalSaving] = useState(false);
  const [modalError, setModalError] = useState(null);

  useEffect(() => {
    if (!org || !user) return;
    loadRoles();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [org?.id, user?.id]);

  async function loadRoles() {
    setLoading(true);
    setErr(null);
    try {
      // fetch all roles for this org (used for supervisor dropdown + optionally showing the owner view)
      const rolesResp = await api.getAllRolesForOrg(org.id, user.id);
      const allRoles = Array.isArray(rolesResp) ? rolesResp : (rolesResp?.data ?? []);
      setRolesAll(allRoles);

      // Keep only the roles in which the current user is a member.
      const rolesWithMembership = [];
      for (const r of allRoles) {
        let isMember = false;
        if (Array.isArray(r.users)) {
          isMember = r.users.some((u) => u?.id === user.id);
        } else {
          // fallback: fetch users for the role and check membership
          try {
            const usersResp = await api.getAllUsersForRole(r.id, user.id);
            const users = Array.isArray(usersResp) ? usersResp : (usersResp?.data ?? []);
            isMember = users.some((u) => u?.id === user.id);
          } catch (e) {
            console.warn('Failed to fetch users for role', r.id, e);
            isMember = false;
          }
        }

        if (isMember) rolesWithMembership.push(r);
      }

      setRolesForUser(rolesWithMembership);
    } catch (e) {
      console.error('Failed to load roles for org', org.id, e);
      setErr(e?.message || 'Failed to load roles');
      setRolesForUser([]);
      setRolesAll([]);
    } finally {
      setLoading(false);
    }
  }

  // Utility: build a candidate user list for the role user-picker.
  // Best-effort: include org owner and any users that already appear on any role.
  function buildCandidateUsers() {
    const m = new Map();
    if (org?.owner) m.set(org.owner.id, org.owner);
    for (const r of rolesAll) {
      if (Array.isArray(r.users)) {
        for (const u of r.users) {
          if (u && u.id) m.set(u.id, u);
        }
      }
    }
    // convert to array
    return Array.from(m.values());
  }

  // Owner check (change this if you want different permission rule)
  const isOwner = user && org && org.owner && user.id === org.owner.id;

  // --- Modal handling ---
  function openCreateModal() {
    setEditingRole(null);
    setModalError(null);
    setRoleModalOpen(true);
  }

  function openEditModal(role) {
    setEditingRole(role);
    setModalError(null);
    setRoleModalOpen(true);
  }

  function closeModal() {
    setEditingRole(null);
    setModalError(null);
    setRoleModalOpen(false);
  }

    async function saveRole(values) {
    setModalSaving(true);
    setModalError(null);

    const roleDto = {
        title: values.title || '',
        workHours: values.workHours || null,
        workDays: values.workDays || null,
        hoursPerWeek:
        typeof values.hoursPerWeek === 'number' ? values.hoursPerWeek : null,
        organization: { id: org.id }
    };

    // -----------------------------
    // Supervisor role handling
    // -----------------------------
    if (editingRole) {
        // EDIT MODE → allow clearing
        if (values.clearSupervisorRole) {
        roleDto.clearSupervisorRole = true;
        } else if (values.supervisorRoleId) {
        roleDto.supervisorRole = { id: values.supervisorRoleId };
        }
        // else → leave unchanged (do nothing)
    } else {
        // CREATE MODE → just set if present
        if (values.supervisorRoleId) {
        roleDto.supervisorRole = { id: values.supervisorRoleId };
        }
    }

    // -----------------------------
    // Users handling
    // -----------------------------
    if (editingRole) {
        if (values.clearUsers) {
        roleDto.clearUsers = true;
        } else if (Array.isArray(values.userIds)) {
        roleDto.users = values.userIds.map((id) => ({ id }));
        }
    } else {
        roleDto.users = Array.isArray(values.userIds)
        ? values.userIds.map((id) => ({ id }))
        : [];
    }

    try {
        if (editingRole && editingRole.id) {
        await api.editRole(editingRole.id, user.id, roleDto);
        } else {
        await api.createRole(user.id, roleDto);
        }

        await loadRoles();
        if (refresh) await refresh();
        closeModal();
    } catch (err) {
        console.error('Save role failed', err);
        setModalError(err?.message || 'Save failed');
    } finally {
        setModalSaving(false);
    }
    }

  async function deleteRole(roleId) {
  if (!window.confirm('Delete this role?')) return;

  try {
    await api.deleteRole(roleId, user.id); // actingUserId required
    await loadRoles(); // refresh roles in this org
    if (refresh) await refresh(); // refresh org list if needed
  } catch (err) {
    console.error('Delete role failed', err);
    alert('Delete failed: ' + (err?.message || err));
  }
}

  // --- render ---
  return (
    <div className="org" style={{ border: '1px solid #ddd', padding: '8px', borderRadius: 6 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <h3 style={{ margin: 0 }}>{org?.name || 'Unnamed organization'}</h3>
        <div style={{ fontSize: '0.9rem', color: '#666' }}>
          Owner: {org?.owner?.username || '—'}
        </div>
      </div>

      <div style={{ marginTop: 8 }}>
        <h4 style={{ margin: '8px 0' }}>Your role{rolesForUser.length !== 1 ? 's' : ''}</h4>

        {loading && <div>Loading roles…</div>}
        {err && <div className="error">Error: {err}</div>}

        {!loading && rolesForUser.length === 0 && <div style={{ color: '#666' }}>You have no role in this organization.</div>}

        <div>
          {rolesForUser.map((r) => (
            <div key={r.id} style={{ marginBottom: 8 }}>
              <RoleDetail role={r} />
              {isOwner && (
                <div style={{ marginTop: 6 }}>
                    <button onClick={() => openEditModal(r)} style={{ marginRight: 6 }}>
                    Edit
                    </button>
                    <button onClick={() => deleteRole(r.id)}>
                    Delete
                    </button>
                </div>
                )}
            </div>
          ))}
        </div>
      </div>

      {/* Owner-only role management */}
      {isOwner && (
        <div style={{ marginTop: 12 }}>
          <button onClick={openCreateModal}>Create role</button>
        </div>
      )}

      {/* refresh */}
      {refresh && (
        <div style={{ marginTop: 8 }}>
          <button onClick={() => { loadRoles(); refresh(); }}>Refresh</button>
        </div>
      )}

      {/* Role Create/Edit Modal */}
      {roleModalOpen && (
        <RoleModal
          org={org}
          editingRole={editingRole}
          rolesAll={rolesAll}
          candidateUsers={buildCandidateUsers()}
          onClose={closeModal}
          onSave={saveRole}
          saving={modalSaving}
          error={modalError}
        />
      )}
    </div>
  );
}

/**
 * Internal small Modal component for Create/Edit Role.
 * Props:
 * - org
 * - editingRole (null => create)
 * - rolesAll (for supervisor dropdown)
 * - candidateUsers: array of user objects { id, username } to pick members from (best-effort)
 * - onClose
 * - onSave(values)
 * - saving, error
 */
function RoleModal({ org, editingRole, rolesAll = [], candidateUsers = [], onClose, onSave, saving, error }) {
  const [title, setTitle] = useState(editingRole?.title || '');
  const [workHours, setWorkHours] = useState(editingRole?.workHours || '');
  const [workDays, setWorkDays] = useState(editingRole?.workDays || '');
  const [hoursPerWeek, setHoursPerWeek] = useState(editingRole?.hoursPerWeek ?? '');
  const [supervisorRoleId, setSupervisorRoleId] = useState(editingRole?.supervisorRole?.id || '');
  const [userIds, setUserIds] = useState(
    Array.isArray(editingRole?.users) ? editingRole.users.map((u) => u.id) : []
  );
  const [localError, setLocalError] = useState(null);

  // simple toggle for multi-select users
  function toggleUser(id) {
    setUserIds((prev) => {
      if (prev.includes(id)) return prev.filter((x) => x !== id);
      return [...prev, id];
    });
  }

  async function handleSave() {
    setLocalError(null);
    if (!title?.trim()) {
      setLocalError('Title is required');
      return;
    }
    // parse hoursPerWeek -> float or null
    const h = hoursPerWeek === '' ? null : Number(hoursPerWeek);
    if (hoursPerWeek !== '' && (isNaN(h) || !isFinite(h))) {
      setLocalError('hoursPerWeek must be a number');
      return;
    }

    const vals = {
      title: title.trim(),
      workHours: workHours === '' ? null : workHours,
      workDays: workDays === '' ? null : workDays,
      hoursPerWeek: h,
      supervisorRoleId: supervisorRoleId || null,
      userIds
    };

    await onSave(vals);
  }

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'rgba(0,0,0,0.4)',
        zIndex: 9999
      }}
    >
      <div style={{ width: 720, maxHeight: '90vh', overflow: 'auto', background: '#fff', padding: 16, borderRadius: 8 }}>
        <h3 style={{ marginTop: 0 }}>{editingRole ? 'Edit role' : `Create role in ${org?.name}`}</h3>

        {(localError || error) && <div style={{ color: 'red' }}>{localError || error}</div>}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          <div>
            <label>
              Title
              <input style={{ width: '100%' }} value={title} onChange={(e) => setTitle(e.target.value)} />
            </label>
          </div>

          <div>
            <label>
              Supervisor role
              <select style={{ width: '100%' }} value={supervisorRoleId || ''} onChange={(e) => setSupervisorRoleId(e.target.value)}>
                <option value="">— none —</option>
                {rolesAll.map((r) => (
                  // do not allow selecting itself as supervisor (if editing)
                  (editingRole && r.id === editingRole.id) ? null : (
                    <option key={r.id} value={r.id}>
                      {r.title} {r.id ? `(${r.id.slice(0,8)})` : ''}
                    </option>
                  )
                ))}
              </select>
            </label>
          </div>

          <div>
            <label>
              Work hours (string)
              <input style={{ width: '100%' }} value={workHours} onChange={(e) => setWorkHours(e.target.value)} />
            </label>
          </div>

          <div>
            <label>
              Work days (string)
              <input style={{ width: '100%' }} value={workDays} onChange={(e) => setWorkDays(e.target.value)} />
            </label>
          </div>

          <div>
            <label>
              Hours per week
              <input style={{ width: '100%' }} value={hoursPerWeek} onChange={(e) => setHoursPerWeek(e.target.value)} />
            </label>
          </div>

          <div>
            <label style={{ display: 'block' }}>
              Organization
              <input style={{ width: '100%' }} value={org?.name || ''} disabled />
            </label>
          </div>
        </div>

        <div style={{ marginTop: 12 }}>
          <strong>Assign users</strong>
          <div style={{ marginTop: 8, maxHeight: 200, overflow: 'auto', border: '1px solid #eee', padding: 8 }}>
            {candidateUsers.length === 0 && <div style={{ color: '#666' }}>No candidate users available.</div>}
            {candidateUsers.map((u) => (
              <label key={u.id} style={{ display: 'block', marginBottom: 4 }}>
                <input
                  type="checkbox"
                  checked={userIds.includes(u.id)}
                  onChange={() => toggleUser(u.id)}
                  style={{ marginRight: 8 }}
                />
                {u.username} {u.id === org?.owner?.id ? '(owner)' : ''}
              </label>
            ))}
          </div>
        </div>

        <div style={{ marginTop: 12, display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button onClick={onClose} disabled={saving}>Cancel</button>
          <button onClick={handleSave} disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
        </div>
      </div>
    </div>
  );
}