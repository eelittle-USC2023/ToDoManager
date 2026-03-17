// src/components/TaskRow.jsx
import React, { useMemo } from 'react';

/**
 * formatDate - friendly formatting for Date objects
 * Falls back to '—' if invalid.
 */
function formatDate(d) {
  if (!d) return '—';
  if (!(d instanceof Date) || isNaN(d)) return '—';
  return d.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  });
}

/**
 * formatDuration - minutes -> human readable "Xh Ym" or "Zm"
 */
function formatDuration(mins) {
  if (mins === null || mins === undefined || mins === '') return '—';
  const m = Number(mins);
  if (isNaN(m) || m < 0) return '—';
  if (m === 0) return '0m';
  const hours = Math.floor(m / 60);
  const rem = m % 60;
  if (hours > 0) {
    return rem > 0 ? `${hours}h ${rem}m` : `${hours}h`;
  }
  return `${rem}m`;
}

/**
 * TaskRow
 * - shows Title, Description, Start, End (dueOffset -> end), Next Start (recurrence), Time to complete, Actions
 */
export default function TaskRow({ task, onEdit = () => {}, onDelete = () => {} }) {
  const { start, end, nextStart, duration } = useMemo(() => {
    let startDate = null;
    let endDate = null;
    let nextDate = null;

    // parse startDateTime (expects ISO / OffsetDateTime string)
    try {
      if (task?.startDateTime) {
        const s = new Date(task.startDateTime);
        if (!isNaN(s)) startDate = s;
      }
    } catch (e) {
      // ignore
    }

    // compute end: start + dueOffsetHours (hours)
    const offsetHours = task?.dueOffsetHours ?? task?.dueOffset ?? null;
    if (startDate && offsetHours !== null && offsetHours !== undefined && offsetHours !== '') {
      const hrs = Number(offsetHours);
      if (!isNaN(hrs)) endDate = new Date(startDate.getTime() + hrs * 3600 * 1000);
    }

    // compute nextStart: start + recurrenceFrequencyHours (hours) — only if recurrence > 0
    const recHours = task?.recurrenceFrequencyHours ?? task?.recurrence ?? null;
    if (startDate && recHours !== null && recHours !== undefined && recHours !== '') {
      const rh = Number(recHours);
      if (!isNaN(rh) && rh > 0) {
        nextDate = new Date(startDate.getTime() + rh * 3600 * 1000);
      }
    }

    // duration in minutes
    const dur = task?.timeToCompleteMinutes ?? task?.timeToComplete ?? task?.timeToCompleteMins ?? null;

    return { start: startDate, end: endDate, nextStart: nextDate, duration: dur };
  }, [task]);

  return (
    <tr>
      <td style={{ padding: '6px 8px', verticalAlign: 'top' }}>{task?.title || '—'}</td>

      <td style={{ padding: '6px 8px', verticalAlign: 'top', maxWidth: 420, whiteSpace: 'pre-wrap' }}>
        {task?.description || '—'}
      </td>

      <td style={{ padding: '6px 8px', verticalAlign: 'top' }}>{formatDate(start)}</td>

      <td style={{ padding: '6px 8px', verticalAlign: 'top' }}>{formatDate(end)}</td>

      <td style={{ padding: '6px 8px', verticalAlign: 'top' }}>{formatDate(nextStart)}</td>

      <td style={{ padding: '6px 8px', verticalAlign: 'top' }}>{formatDuration(duration)}</td>

      <td style={{ padding: '6px 8px', verticalAlign: 'top' }}>
        <button onClick={() => onEdit(task)}>Edit</button>{' '}
        <button onClick={() => onDelete(task.id)}>Delete</button>
      </td>
    </tr>
  );
}