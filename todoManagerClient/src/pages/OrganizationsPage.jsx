// src/pages/OrganizationsPage.jsx
import React, { useEffect, useState } from 'react';
import api from '../api';
import { useAuth } from '../authContext';
import OrgDetail from '../components/OrgDetail';

export default function OrganizationsPage() {
  const { user } = useAuth();
  const [orgs, setOrgs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Create form state
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState('');
  const [createError, setCreateError] = useState(null);

  // Edit form state (inline editing)
  const [editingId, setEditingId] = useState(null);
  const [editName, setEditName] = useState('');
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState(null);

  useEffect(() => {
    if (!user) {
      setOrgs([]);
      return;
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await api.getAllOrganizationsForUser(user.id);
      setOrgs(Array.isArray(res) ? res : (res?.data ?? []));
    } catch (err) {
      console.error('Failed loading organizations', err);
      setError(err?.message || 'Failed loading organizations');
      setOrgs([]);
    } finally {
      setLoading(false);
    }
  }

  // CREATE
  async function handleCreate(e) {
    e.preventDefault();
    setCreateError(null);
    if (!newName?.trim()) {
      setCreateError('Name is required');
      return;
    }
    setCreating(true);
    try {
      // owner is current user
      await api.createOrganization({ ownerId: user.id, name: newName.trim() });
      setNewName('');
      await load();
    } catch (err) {
      console.error('Create organization failed', err);
      setCreateError(err?.message || 'Create failed');
    } finally {
      setCreating(false);
    }
  }

  // START EDIT
  function beginEdit(org) {
    setEditingId(org.id);
    setEditName(org.name || '');
    setEditError(null);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditName('');
    setEditError(null);
  }

  // SAVE EDIT
  async function saveEdit() {
    if (!editingId) return;
    if (!editName?.trim()) {
      setEditError('Name is required');
      return;
    }
    setEditSaving(true);
    setEditError(null);
    try {
      // per your API design: only name is sent (empty string clears)
      await api.editOrganization(editingId, user.id, { name: editName.trim() });
      cancelEdit();
      await load();
    } catch (err) {
      console.error('Save organization failed', err);
      setEditError(err?.message || 'Save failed');
    } finally {
      setEditSaving(false);
    }
  }

  // DELETE
  async function handleDelete(id) {
    if (!confirm('Delete this organization? This will remove its roles and associations.')) return;
    try {
      await api.deleteOrganization(id, user.id);
      await load();
    } catch (err) {
      console.error('Delete failed', err);
      alert('Delete failed: ' + (err?.message || err));
    }
  }

  if (!user) return <div>Please log in to view your organizations.</div>;

  return (
    <div style={{ padding: 12 }}>
      <h2>Your organizations</h2>

      {/* CREATE */}
      <div style={{ marginBottom: 16, border: '1px solid #ddd', padding: 12, borderRadius: 6 }}>
        <h3 style={{ marginTop: 0 }}>Create organization</h3>
        {createError && <div style={{ color: 'red' }}>{createError}</div>}
        <form onSubmit={handleCreate} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input
            placeholder="Organization name"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            style={{ flex: '1 1 300px' }}
          />
          <button type="submit" disabled={creating}>
            {creating ? 'Creating…' : 'Create'}
          </button>
        </form>
      </div>

      {loading && <div>Loading...</div>}
      {error && <div style={{ color: 'red' }}>Error: {error}</div>}

      {!loading && orgs.length === 0 && <div>You are not a member of any organizations.</div>}

      <ul style={{ listStyle: 'none', padding: 0 }}>
        {orgs.map((o) => (
          <li key={o.id} style={{ marginBottom: 12, border: '1px solid #eee', padding: 10, borderRadius: 6 }}>
            {editingId === o.id ? (
              <div>
                {editError && <div style={{ color: 'red' }}>{editError}</div>}
                <input
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  style={{ width: '60%', marginRight: 8 }}
                />
                <button onClick={saveEdit} disabled={editSaving}>
                  {editSaving ? 'Saving…' : 'Save'}
                </button>{' '}
                <button onClick={cancelEdit}>Cancel</button>
              </div>
            ) : (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <strong style={{ fontSize: 16 }}>{o.name}</strong>
                    <div style={{ color: '#555', fontSize: 13 }}>
                      Owner: {o.owner?.username ?? '—'}
                    </div>
                  </div>
                  <div>
                    <button onClick={() => beginEdit(o)} style={{ marginRight: 8 }}>
                      Edit
                    </button>
                    <button onClick={() => handleDelete(o.id)}>Delete</button>
                  </div>
                </div>

                {/* OrgDetail shows roles & users — reuse your existing component */}
                <div style={{ marginTop: 8 }}>
                  <OrgDetail org={o} refresh={load} />
                </div>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}