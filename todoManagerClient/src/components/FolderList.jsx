import React, { useState } from 'react';
import api from '../api';

export default function FolderList({ userId, folders = [], refresh = () => {} }) {
  // create form
  const [newTitle, setNewTitle] = useState('');
  const [newNote, setNewNote] = useState('');
  const [creating, setCreating] = useState(false);

  // edit form
  const [editingId, setEditingId] = useState(null);
  const [editTitle, setEditTitle] = useState('');
  const [editNote, setEditNote] = useState('');
  const [savingEdit, setSavingEdit] = useState(false);

  async function createFolder() {
    if (!newTitle || !userId) return;
    setCreating(true);
    try {
      await api.createFolder({ userId, title: newTitle, note: newNote });
      setNewTitle('');
      setNewNote('');
      await refresh();
    } catch (err) {
      console.error('Create folder failed', err);
      alert('Create folder failed: ' + (err?.message || err));
    } finally {
      setCreating(false);
    }
  }

  function startEdit(folder) {
    setEditingId(folder.id);
    setEditTitle(folder.title || '');
    setEditNote(folder.note || '');
  }

  function cancelEdit() {
    setEditingId(null);
    setEditTitle('');
    setEditNote('');
  }

  async function saveEdit() {
    if (!editingId) return;
    setSavingEdit(true);
    try {
      // api.editFolder(id, { title, note })
      await api.editFolder(editingId, { title: editTitle, note: editNote });
      cancelEdit();
      await refresh();
    } catch (err) {
      console.error('Save folder failed', err);
      alert('Save failed: ' + (err?.message || err));
    } finally {
      setSavingEdit(false);
    }
  }

  async function doDelete(id) {
    if (!confirm('Delete this folder?')) return;
    try {
      await api.deleteFolder(id);
      await refresh();
    } catch (err) {
      console.error('Delete failed', err);
      alert('Delete failed: ' + (err?.message || err));
    }
  }

  return (
    <div style={{ border: '1px solid #ddd', padding: 12, borderRadius: 6, marginBottom: 12 }}>
      <h3 style={{ marginTop: 0 }}>Folders</h3>

      <ul style={{ paddingLeft: 18 }}>
        {folders.map((f) => (
          <li key={f.id} style={{ marginBottom: 6 }}>
            {editingId === f.id ? (
              <div>
                <input
                  style={{ width: '40%', marginRight: 8 }}
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  placeholder="Title"
                />
                <input
                  style={{ width: '40%', marginRight: 8 }}
                  value={editNote}
                  onChange={(e) => setEditNote(e.target.value)}
                  placeholder="Note"
                />
                <button onClick={saveEdit} disabled={savingEdit}>
                  {savingEdit ? 'Saving…' : 'Save'}
                </button>{' '}
                <button onClick={cancelEdit}>Cancel</button>
              </div>
            ) : (
              <div>
                <strong>{f.title || '—'}</strong>
                {f.note ? <span style={{ marginLeft: 8, color: '#555' }}> — {f.note}</span> : null}
                <div style={{ display: 'inline-block', marginLeft: 12 }}>
                  <button onClick={() => startEdit(f)} style={{ marginRight: 6 }}>
                    Edit
                  </button>
                  <button onClick={() => doDelete(f.id)}>Delete</button>
                </div>
              </div>
            )}
          </li>
        ))}
        {folders.length === 0 && <li style={{ color: '#666' }}>No folders yet</li>}
      </ul>

      <div style={{ marginTop: 10, display: 'flex', gap: 8, alignItems: 'center' }}>
        <input
          placeholder="New folder title"
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          style={{ width: '30%' }}
        />
        <input
          placeholder="Note (optional)"
          value={newNote}
          onChange={(e) => setNewNote(e.target.value)}
          style={{ width: '40%' }}
        />
        <button onClick={createFolder} disabled={creating || !newTitle || !userId}>
          {creating ? 'Creating…' : 'Create'}
        </button>
      </div>
    </div>
  );
}