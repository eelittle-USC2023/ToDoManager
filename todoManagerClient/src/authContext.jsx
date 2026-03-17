// src/authContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';
import api from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  // hydrate from localStorage on mount
  useEffect(() => {
    try {
      const rawUser = localStorage.getItem('user');
      if (rawUser) {
        setUser(JSON.parse(rawUser));
      }
    } catch (e) {
      console.warn('Failed to parse stored user', e);
    }
  }, []);

  // login will call api.login and expect { user } normalized response
  async function login(username, password) {
    const normalized = await api.login({ username, password });
    const actualUser = normalized?.user ?? null;
    if (!actualUser) {
      throw new Error('Login failed: no user returned from server');
    }
    // persist user for refreshes
    localStorage.setItem('user', JSON.stringify(actualUser));
    setUser(actualUser);
    return actualUser;
  }

  function logout() {
    localStorage.removeItem('user');
    setUser(null);
  }

  return <AuthContext.Provider value={{ user, setUser, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}