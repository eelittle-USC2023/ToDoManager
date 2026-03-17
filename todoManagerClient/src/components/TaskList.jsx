// src/components/TaskList.jsx
import React from 'react';
import TaskRow from './TaskRow.jsx';

export default function TaskList({ tasks = [], onEdit = () => {}, onDelete = () => {} }) {
  return (
    <div style={{ marginTop: 8 }}>
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
          {tasks.map((t) => (
            <TaskRow key={t.id} task={t} onEdit={onEdit} onDelete={onDelete} />
          ))}
        </tbody>
      </table>
    </div>
  );
}