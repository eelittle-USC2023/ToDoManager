import React, { useEffect, useState } from 'react';
import api from '../api';
import { useAuth } from '../authContext';

/**
 * RoleDetail
 * Props:
 *  - role: role object
 *  - onEdit?: optional callback(role) to request edit (OrgDetail will pass it if owner)
 */
export default function RoleDetail({ role, onEdit }) {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);

  useEffect(() => {
    if (!role?.id) return;
    let mounted = true;
    api
      .getAllUsersForRole(role.id, user.id)
      .then((data) => {
        const arr = Array.isArray(data) ? data : (data?.data ?? []);
        if (mounted) setUsers(arr);
      })
      .catch(() => {
        if (mounted) setUsers([]);
      });
    return () => {
      mounted = false;
    };
  }, [role]);

  return (
    <div className="role" style={{ border: '1px solid #f0f0f0', padding: 8, borderRadius: 6 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'top' }}>
        <div>
          <strong>{role?.title}</strong>
          <div style={{ fontSize: 12, color: '#555' }}>
            Organization: {role?.organization?.name || '—'}
            <br />
            Supervisor: {role?.supervisorRole?.title || '—'}
            <br />
            Work hours: {role?.workHours || '—'}
            <br />
            Work days: {role?.workDays || '—'}
            <br />
            Hours/week: {role?.hoursPerWeek ?? '—'}
          </div>
        </div>

        {onEdit && (
          <div>
            <button onClick={() => onEdit(role)}>Edit</button>
          </div>
        )}
      </div>

      <div style={{ marginTop: 8 }}>
        <div style={{ fontSize: 13 }}>Users:</div>
        <ul style={{ marginTop: 6 }}>
          {users.map((u) => (
            <li key={u.id}>{u.username}</li>
          ))}
          {users.length === 0 && <li style={{ color: '#666' }}>No users</li>}
        </ul>
      </div>
    </div>
  );
}