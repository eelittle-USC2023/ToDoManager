// src/App.jsx
import React from 'react';
import { Routes, Route, Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from './authContext';

import LoginPage from './pages/LoginPage';
import TasksPage from './pages/TasksPage';
import OrganizationsPage from './pages/OrganizationsPage';
import AccountPage from './pages/AccountPage';

function PrivateRoute({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  if (!user) return null; // don't show navbar if not logged in

  return (
    <nav style={{ marginBottom: '20px' }}>
      <Link to="/tasks" style={{ marginRight: '10px' }}>Tasks</Link>
      <Link to="/organizations" style={{ marginRight: '10px' }}>Organizations</Link>
      <Link to="/account" style={{ marginRight: '10px' }}>Account</Link>
      <button onClick={handleLogout}>Logout</button>
    </nav>
  );
}

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route
          path="/tasks"
          element={
            <PrivateRoute>
              <TasksPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/organizations"
          element={
            <PrivateRoute>
              <OrganizationsPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/account"
          element={
            <PrivateRoute>
              <AccountPage />
            </PrivateRoute>
          }
        />

        <Route path="/" element={<Navigate to="/tasks" replace />} />
      </Routes>
    </>
  );
}