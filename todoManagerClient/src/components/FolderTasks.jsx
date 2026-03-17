// src/components/FolderTasks.jsx
import React, { useMemo } from 'react';
import TaskRow from './TaskRow.jsx';

/**
 * FolderTasks
 * - folders: array of folder objects { id, title, note, ... }
 * - tasks: array of tasks which may have associatedFolder (or associatedFolderId)
 */
export default function FolderTasks({ folders = [], tasks = [], onEdit = () => {}, onDelete = () => {} }) {
  const tasksByFolder = useMemo(() => {
    const map = new Map();
    for (const f of folders) map.set(f.id, []);
    const orphan = [];
    for (const t of tasks) {
      const fid = t.associatedFolder?.id ?? t.associatedFolderId ?? t.folderId ?? null;
      if (fid && map.has(fid)) map.get(fid).push(t);
      else orphan.push(t);
    }
    return { map, orphan };
  }, [folders, tasks]);

  return (
    <div>
      <h3>Folders</h3>
      {folders.length === 0 && <div>No folders</div>}
      {folders.map((f) => {
        const folderTasks = tasksByFolder.map.get(f.id) || [];
        return (
          <details key={f.id} style={{ marginBottom: 8 }}>
            <summary>
              {f.title} ({folderTasks.length})
               - {f.note}
            </summary>
            {folderTasks.length === 0 ? (
              <div style={{ paddingLeft: 12 }}>No tasks in this folder</div>
            ) : (
              <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 8 }}>
                <thead>
                  <tr>
                    <th style={{ textAlign: 'left' }}>Title</th>
                    <th style={{ textAlign: 'left' }}>Description</th>
                    <th style={{ textAlign: 'left' }}>Start</th>
                    <th style={{ textAlign: 'left' }}>End</th>
                    <th style={{ textAlign: 'left' }}>Next start</th>
                    <th style={{ textAlign: 'left' }}>Time to complete</th>
                    <th style={{ textAlign: 'left' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {folderTasks.map((t) => (
                    <TaskRow key={t.id} task={t} onEdit={onEdit} onDelete={onDelete} />
                  ))}
                </tbody>
              </table>
            )}
          </details>
        );
      })}

      <div style={{ marginTop: 12 }}>
        <h4>Tasks not in a folder</h4>
        {tasksByFolder.orphan.length === 0 ? (
          <div>None</div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={{ textAlign: 'left' }}>Title</th>
                <th style={{ textAlign: 'left' }}>Description</th>
                <th style={{ textAlign: 'left' }}>Start</th>
                <th style={{ textAlign: 'left' }}>End</th>
                <th style={{ textAlign: 'left' }}>Next start</th>
                <th style={{ textAlign: 'left' }}>Time to complete</th>
                <th style={{ textAlign: 'left' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {tasksByFolder.orphan.map((t) => (
                <TaskRow key={t.id} task={t} onEdit={onEdit} onDelete={onDelete} />
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}