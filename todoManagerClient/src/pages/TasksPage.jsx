import React, { useEffect, useState } from 'react';
import api from '../api';
import { useAuth } from '../authContext';
import TaskList from '../components/TaskList';
import TaskEditor from '../components/TaskEditor';
import FolderTasks from '../components/FolderTasks';
import FolderList from '../components/FolderList';

export default function TasksPage() {
  const { user } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [folders, setFolders] = useState([]);
  const [editing, setEditing] = useState(null);

  useEffect(() => {
    if (!user?.id) return;
    fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  async function fetchAll() {
    try {
      const t = await api.getTasksByUser(user.id);
      // API wrapper may return array or { data: [...] }
      setTasks(Array.isArray(t) ? t : t?.data ?? []);

      const f = await api.getFoldersByUser(user.id);
      setFolders(Array.isArray(f) ? f : f?.data ?? []);
    } catch (err) {
      console.error('Failed loading tasks/folders', err);
      setTasks([]);
      setFolders([]);
    }
  }

  async function deleteTask(id) {
    try {
      await api.deleteTask(id, user.id);
      setTasks((s) => s.filter((x) => x.id !== id));
    } catch (err) {
      console.error('Delete failed', err);
    }
  }

  return (
    <div style={{ padding: 16 }}>
      <h2>Your tasks</h2>

      {/* FolderTasks shows tasks grouped by folder / parent-child */}
      <FolderTasks
        folders={folders}
        tasks={tasks}
        onEdit={(t) => setEditing(t)}
        onDelete={deleteTask}
      />

      {/* Folder management UI inserted here, between FolderTasks and the flat TaskList */}
      <div style={{ marginTop: 12 }}>
        <FolderList userId={user.id} folders={folders} refresh={fetchAll} />
      </div>

      <div style={{ marginTop: 12 }}>
        <h3>All tasks (flat view)</h3>
        <button onClick={() => setEditing({})}>Create task</button>
        <TaskList tasks={tasks} onEdit={(t) => setEditing(t)} onDelete={deleteTask} />
      </div>

      {editing && (
        <TaskEditor
          task={editing}
          folders={folders}
          onClose={() => setEditing(null)}
          onSaved={fetchAll}
          userId={user.id}
        />
      )}
    </div>
  );
}