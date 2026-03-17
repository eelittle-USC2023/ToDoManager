// src/components/TaskEditor.jsx
import React, { useEffect, useState } from 'react';
import api from '../api';

/**
 * TaskEditor
 *
 * Props:
 *  - task: existing task object (optional)
 *  - onClose: fn()
 *  - onSaved: fn()
 *  - userId: UUID of the signed-in user (required)
 *  - folders: optional array of folder objects for folder dropdown (if not provided, the component will attempt to load them via API)
 */
function isoLocalDatetimeString(d) {
  if (!d) return '';
  const dt = new Date(d);
  if (isNaN(dt)) return '';
  const pad = (n) => String(n).padStart(2, '0');
  const yyyy = dt.getFullYear();
  const mm = pad(dt.getMonth() + 1);
  const dd = pad(dt.getDate());
  const hh = pad(dt.getHours());
  const min = pad(dt.getMinutes());
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
}

export default function TaskEditor({
  task = {},
  onClose = () => {},
  onSaved = () => {},
  userId,
  folders: initialFolders = null
}) {
  // Basic text fields
  const [title, setTitle] = useState(task.title || '');
  const [description, setDescription] = useState(task.description || '');

  // start/end as datetime-local strings
  const initialStart = task.startDateTime ? isoLocalDatetimeString(task.startDateTime) : isoLocalDatetimeString(new Date());
  const [startLocal, setStartLocal] = useState(initialStart);

  const computeInitialEnd = () => {
    try {
      if (task.startDateTime && (task.dueOffsetHours || task.dueOffsetHours === 0)) {
        const s = new Date(task.startDateTime);
        const end = new Date(s.getTime() + Number(task.dueOffsetHours) * 3600 * 1000);
        return isoLocalDatetimeString(end);
      }
    } catch (e) {
      // ignore
    }
    // default: start + 1 hour
    try {
      const s = startLocal ? new Date(startLocal) : new Date();
      const end = new Date(s.getTime() + 60 * 60 * 1000);
      return isoLocalDatetimeString(end);
    } catch (e) {
      return isoLocalDatetimeString(new Date());
    }
  };
  const [endLocal, setEndLocal] = useState(computeInitialEnd());

  // recurrence fractional hours and time to complete
  const [recurrenceFrequencyHours, setRecurrenceFrequencyHours] = useState(
    task.recurrenceFrequencyHours ?? task.recurrence ?? 0
  );
  const [timeToCompleteMinutes, setTimeToCompleteMinutes] = useState(
    task.timeToCompleteMinutes ?? task.timeToComplete ?? 0
  );

  // parent task select + explicit clear flag (for edits)
  const [parentTaskId, setParentTaskId] = useState(task.parentTask?.id ?? task.parentTaskId ?? '');
  const [clearParentTask, setClearParentTask] = useState(false);

  // associated folder select + explicit clear flag (for edits)
  const [associatedFolderId, setAssociatedFolderId] = useState(task.associatedFolderId ?? '');
  const [clearAssociatedFolder, setClearAssociatedFolder] = useState(false);

  // tasks and folders to choose from
  const [userTasks, setUserTasks] = useState([]);
  const [folders, setFolders] = useState(initialFolders || []);
  const [loadingTasks, setLoadingTasks] = useState(false);
  const [loadingFolders, setLoadingFolders] = useState(false);

  // load user's tasks for parent dropdown (exclude current task)
  useEffect(() => {
    if (!userId) return;
    let mounted = true;
    setLoadingTasks(true);
    api
      .getTasksByUser(userId)
      .then((res) => {
        const arr = Array.isArray(res) ? res : res?.data ?? [];
        if (!mounted) return;
        const filtered = arr.filter((t) => !task.id || t.id !== task.id);
        setUserTasks(filtered);
      })
      .catch((err) => {
        console.warn('Failed to load user tasks', err);
        if (mounted) setUserTasks([]);
      })
      .finally(() => {
        if (mounted) setLoadingTasks(false);
      });
    return () => {
      mounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId, task.id]);

  // optionally load folders if not passed in
  useEffect(() => {
    if (initialFolders) return; // provided by parent; don't reload
    if (!userId) return;
    let mounted = true;
    setLoadingFolders(true);
    api
      .getFoldersByUser(userId)
      .then((res) => {
        const arr = Array.isArray(res) ? res : res?.data ?? [];
        if (mounted) setFolders(arr);
      })
      .catch((err) => {
        console.warn('Failed to load folders', err);
        if (mounted) setFolders([]);
      })
      .finally(() => {
        if (mounted) setLoadingFolders(false);
      });
    return () => {
      mounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  // recompute a sane end when start changes (only if end is empty)
  useEffect(() => {
    if (!endLocal) {
      const s = startLocal ? new Date(startLocal) : new Date();
      const end = new Date(s.getTime() + 60 * 60 * 1000);
      setEndLocal(isoLocalDatetimeString(end));
    }
  }, [startLocal]); // eslint-disable-line

  async function save(e) {
    e?.preventDefault?.();

    if (!title || title.trim() === '') {
      alert('Title is required');
      return;
    }

    const startDate = startLocal ? new Date(startLocal) : null;
    const endDate = endLocal ? new Date(endLocal) : null;
    if (!startDate || isNaN(startDate)) {
      alert('Start date is invalid');
      return;
    }
    if (!endDate || isNaN(endDate)) {
      alert('End date is invalid');
      return;
    }
    if (endDate.getTime() < startDate.getTime()) {
      alert('End date must be the same or after the start date');
      return;
    }

    const dueOffsetHours = (endDate.getTime() - startDate.getTime()) / (1000 * 3600);

    // Build payload: include only fields that indicate intent.
    const payload = {
      // include assigned user to satisfy server checks
      user: { id: userId },

      title: title.trim(),
      description: description || '',
      startDateTime: new Date(startDate).toISOString(),
      dueOffsetHours: Number(Number(dueOffsetHours).toFixed(6)),
      recurrenceFrequencyHours: recurrenceFrequencyHours === '' ? 0 : Number(recurrenceFrequencyHours),
      timeToCompleteMinutes: timeToCompleteMinutes === '' ? 0 : Number(timeToCompleteMinutes)
    };

    // Associated folder semantics (edit-only boolean to clear; set when chosen)
    if (task.id) {
      // editing: include clear flag if checked
      if (clearAssociatedFolder) {
        payload.clearAssociatedFolder = true;
      } else {
        payload.clearAssociatedFolder = false;
      }
      // include associatedFolder only if user selected one (non-empty)
      if (associatedFolderId) {
        payload.associatedFolderId = associatedFolderId;
      }
    } else {
      // creating: if a folder chosen, set it; otherwise omit (no explicit clear needed)
      if (associatedFolderId) {
        payload.associatedFolderId = associatedFolderId;
      }
    }

    // Parent task semantics (similar boolean clear)
    if (task.id) {
      if (clearParentTask) {
        payload.clearParentTask = true;
      } else {
        payload.clearParentTask = false;
      }
      if (parentTaskId) {
        payload.parentTaskId = parentTaskId;
      }
    } else {
      if (parentTaskId) {
        payload.parentTaskId = parentTaskId;
      }
    }

    try {
      if (task.id) {
        await api.editTask(task.id, userId, payload);
      } else {
        await api.createTask(userId, payload);
      }
      onSaved();
      // reset local state after save
      setTitle('');
      setDescription('');
      setStartLocal(isoLocalDatetimeString(new Date()));
      setEndLocal(isoLocalDatetimeString(new Date(Date.now() + 60 * 60 * 1000)));
      setRecurrenceFrequencyHours(0);
      setTimeToCompleteMinutes(0);
      setParentTaskId('');
      setClearParentTask(false);
      setAssociatedFolderId('');
      setClearAssociatedFolder(false);
      onClose();
    } catch (err) {
      console.error('Save failed', err);
      alert('Save failed: ' + (err?.message || err));
    }
  }

  return (
    <div className="modal" style={{ padding: 12, maxWidth: 800 }}>
      <h3 style={{ marginTop: 0 }}>{task.id ? 'Edit Task' : 'New Task'}</h3>
      <form onSubmit={save}>
        <div style={{ marginBottom: 8 }}>
          <label>
            Title
            <br />
            <input value={title} onChange={(e) => setTitle(e.target.value)} style={{ width: '100%' }} />
          </label>
        </div>

        <div style={{ marginBottom: 8 }}>
          <label>
            Description
            <br />
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={4}
              style={{ width: '100%' }}
            />
          </label>
        </div>

        <div style={{ display: 'flex', gap: 12, marginBottom: 8 }}>
          <label style={{ flex: 1 }}>
            Start date/time
            <br />
            <input
              type="datetime-local"
              value={startLocal}
              onChange={(e) => setStartLocal(e.target.value)}
              style={{ width: '100%' }}
            />
          </label>

          <label style={{ flex: 1 }}>
            End date/time
            <br />
            <input
              type="datetime-local"
              value={endLocal}
              onChange={(e) => setEndLocal(e.target.value)}
              style={{ width: '100%' }}
            />
          </label>
        </div>

        <div style={{ display: 'flex', gap: 12, marginBottom: 8 }}>
          <label style={{ flex: 1 }}>
            Recurrence frequency (hours)
            <br />
            <input
                type="number"
                min="0"
                step="1"
                value={recurrenceFrequencyHours}
                onChange={(e) => setRecurrenceFrequencyHours(e.target.value === '' ? '' : Number(e.target.value))}
                style={{ width: '100%' }}
            />
          </label>

          <label style={{ flex: 1 }}>
            Time to complete (minutes)
            <br />
            <input
              type="number"
              min="0"
              step="1"
              value={timeToCompleteMinutes}
              onChange={(e) => setTimeToCompleteMinutes(e.target.value === '' ? '' : Number(e.target.value))}
              style={{ width: '100%' }}
            />
          </label>
        </div>

        <div style={{ marginBottom: 8 }}>
          <label>
            Associated folder (optional)
            <br />
            <select
              value={associatedFolderId}
              onChange={(e) => setAssociatedFolderId(e.target.value === '' ? '' : e.target.value)}
              disabled={loadingFolders}
              style={{ width: '100%' }}
            >
              <option value="">— none —</option>
              {folders.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.title || f.name || f.id}
                </option>
              ))}
            </select>
          </label>

          {/* show clear checkbox only when editing */}
          {task.id && (
            <div style={{ marginTop: 6 }}>
              <label>
                <input
                  type="checkbox"
                  checked={clearAssociatedFolder}
                  onChange={(e) => setClearAssociatedFolder(e.target.checked)}
                />
                {' '}Clear associated folder (explicit)
              </label>
            </div>
          )}
          {loadingFolders && <div style={{ fontSize: 12 }}>Loading folders…</div>}
        </div>

        <div style={{ marginBottom: 12 }}>
          <label>
            Parent task (optional)
            <br />
            <select
              value={parentTaskId}
              onChange={(e) => setParentTaskId(e.target.value === '' ? '' : e.target.value)}
              disabled={loadingTasks}
              style={{ width: '100%' }}
            >
              <option value="">— none —</option>
              {userTasks.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.title || t.id}
                </option>
              ))}
            </select>
          </label>

          {task.id && (
            <div style={{ marginTop: 6 }}>
              <label>
                <input
                  type="checkbox"
                  checked={clearParentTask}
                  onChange={(e) => setClearParentTask(e.target.checked)}
                />
                {' '}Clear parent task (explicit)
              </label>
            </div>
          )}
        </div>

        <div style={{ display: 'flex', gap: 8 }}>
          <button type="submit">{task.id ? 'Save changes' : 'Create task'}</button>
          <button
            type="button"
            onClick={() => {
              onClose();
            }}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}