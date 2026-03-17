import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../authContext';
import api from '../api';

export default function AccountPage() {
  const { user, setUser, logout } = useAuth();
  const nav = useNavigate();

  const [username, setUsername] = useState(user?.username ?? '');
  const [password, setPassword] = useState(null);   // always null initially
  const [loadingSave, setLoadingSave] = useState(false);
  const [saveError, setSaveError] = useState(null);
  const [saveSuccess, setSaveSuccess] = useState(null);

  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  useEffect(() => {
    setUsername(user?.username ?? '');
    setPassword(null); // ensure password is cleared when page loads
  }, [user?.id]);

  if (!user) {
    return (
      <div style={{ padding: 16 }}>
        <h2>Account</h2>
        <div>You must be signed in to view this page.</div>
      </div>
    );
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaveError(null);
    setSaveSuccess(null);

    if (!username) {
      setSaveError('Username is required');
      return;
    }

    setLoadingSave(true);

    try {
      await api.editUser(user.id, { username, password });

      const newUser = {
        id: user.id,
        username
      };

      setUser(newUser);
      localStorage.setItem('user', JSON.stringify(newUser));

      setSaveSuccess('Account updated');

      // reset password after save
      setPassword(null);

    } catch (err) {
      console.error('Save failed', err);
      setSaveError(err.message || 'Failed to save account');
    } finally {
      setLoadingSave(false);
    }
  }

  async function handleDelete() {
    setDeleteError(null);

    const ok = window.confirm(
      'Are you sure you want to permanently delete your account?'
    );
    if (!ok) return;

    setDeleting(true);

    try {
      await api.deleteUser(user.id);
      logout();
      nav('/login');
    } catch (err) {
      console.error('Delete failed', err);
      setDeleteError(err.message || 'Failed to delete account');
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div style={{ maxWidth: 640, margin: '0 auto', padding: 16 }}>
      <h2>Account</h2>

      <div style={{ marginBottom: 12 }}>
        <strong>Signed in as:</strong> {user.username}
      </div>

      <div style={{ border: '1px solid #ddd', padding: 12, borderRadius: 6 }}>
        <h3>Edit account</h3>

        {saveError && <div style={{ color: 'red' }}>{saveError}</div>}
        {saveSuccess && <div style={{ color: 'green' }}>{saveSuccess}</div>}

        <form onSubmit={handleSave} style={{ display: 'grid', gap: 8 }}>
          <label>
            Username
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </label>

          <label>
            New password (leave blank to keep current)
            <input
              type="password"
              value={password ?? ''}   // map null → empty string for input
              onChange={(e) =>
                setPassword(e.target.value === '' ? null : e.target.value)
              }
            />
          </label>

          <button type="submit" disabled={loadingSave}>
            {loadingSave ? 'Saving…' : 'Save changes'}
          </button>
        </form>
      </div>

      <div
        style={{
          marginTop: 16,
          border: '1px solid #f4cccc',
          background: '#fff6f6',
          padding: 12,
          borderRadius: 6
        }}
      >
        <h3 style={{ marginTop: 0 }}>Danger zone</h3>

        {deleteError && <div style={{ color: 'red' }}>{deleteError}</div>}

        <button
          onClick={handleDelete}
          disabled={deleting}
          style={{
            color: 'white',
            background: '#d9534f',
            border: 'none',
            padding: '8px 12px',
            borderRadius: 4
          }}
        >
          {deleting ? 'Deleting…' : 'Delete account'}
        </button>
      </div>
    </div>
  );
}