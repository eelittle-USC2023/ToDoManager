// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../authContext';
import api from '../api';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { login, setUser } = useAuth();
  const nav = useNavigate();
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  // Create-account UI state
  const [createMode, setCreateMode] = useState(false);
  const [createUsername, setCreateUsername] = useState('');
  const [createPassword, setCreatePassword] = useState('');
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);

  async function handleLogin(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(username, password);
      nav('/tasks');
    } catch (err) {
      console.error('Login failed', err);
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(e) {
    e.preventDefault();
    setCreateError(null);

    if (!createUsername || !createPassword) {
      setCreateError('Username and password are required');
      return;
    }

    setCreateLoading(true);
    try {
      const created = await api.createUser(createUsername, createPassword);
      // created might be the created user object (depends on backend).
      // Try to log in using the normal flow for consistency.
      try {
        await login(createUsername, createPassword);
        nav('/tasks');
        return;
      } catch (loginErr) {
        // If login fails but create succeeded, fall back to setting created user directly.
        console.warn('Automatic login after create failed; falling back to using created user', loginErr);
        if (created && (created.id || created.uuid || created.username)) {
          const actualUser = created.id || created.uuid ? {
            id: created.id ?? created.uuid,
            username: created.username ?? createUsername
          } : { username: createUsername };
          // use setUser from context and persist
          try {
            setUser(actualUser);
            localStorage.setItem('user', JSON.stringify(actualUser));
            nav('/tasks');
            return;
          } catch (setErr) {
            console.error('Failed to set user after create', setErr);
            setCreateError('Account created but auto-login failed. Please sign in manually.');
          }
        } else {
          setCreateError('Account created but login info could not be derived. Please sign in.');
        }
      }
    } catch (err) {
      console.error('Create account failed', err);
      setCreateError(err.message || 'Create account failed');
    } finally {
      setCreateLoading(false);
    }
  }

  function openCreate() {
    setCreateMode(true);
    setCreateError(null);
    setCreateUsername('');
    setCreatePassword('');
  }

  function cancelCreate() {
    setCreateMode(false);
    setCreateError(null);
  }

  return (
    <div style={{ maxWidth: 480, margin: '0 auto', padding: 16 }}>
      <h2>Sign in</h2>

      {error && <div style={{ color: 'red', marginBottom: 8 }}>{error}</div>}

      <form onSubmit={handleLogin} style={{ display: 'grid', gap: 8 }}>
        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} />
        </label>

        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        </label>

        <div>
          <button type="submit" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>{' '}
          <button type="button" onClick={openCreate} disabled={createMode}>
            Create account
          </button>
        </div>
      </form>

      {createMode && (
        <div style={{ marginTop: 20, padding: 12, border: '1px solid #ddd', borderRadius: 6 }}>
          <h3>Create account</h3>
          {createError && <div style={{ color: 'red', marginBottom: 8 }}>{createError}</div>}
          <form onSubmit={handleCreate} style={{ display: 'grid', gap: 8 }}>
            <label>
              Desired username
              <input value={createUsername} onChange={(e) => setCreateUsername(e.target.value)} />
            </label>

            <label>
              Desired password
              <input type="password" value={createPassword} onChange={(e) => setCreatePassword(e.target.value)} />
            </label>

            <div>
              <button type="submit" disabled={createLoading}>
                {createLoading ? 'Creating…' : 'Create account'}
              </button>{' '}
              <button type="button" onClick={cancelCreate} disabled={createLoading}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}