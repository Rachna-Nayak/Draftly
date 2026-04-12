import { useEffect, useMemo, useState } from 'react';
import {
  completeReviewSchedule,
  createReviewSchedule,
  getReviewSchedules,
  getReviewSchedulesByReviewer,
} from '../api';

function formatDate(value) {
  if (!value) return 'N/A';
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 'N/A' : parsed.toLocaleString();
}

export default function ReviewerAssignment() {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [filterReviewerId, setFilterReviewerId] = useState('');
  const [form, setForm] = useState({
    conferenceId: '',
    submissionId: '',
    reviewerUserId: '',
    scheduledAt: '',
    dueAt: '',
    adminUserId: '',
  });

  const loadSchedules = async () => {
    setLoading(true);
    setError('');
    try {
      const response = filterReviewerId.trim()
        ? await getReviewSchedulesByReviewer(filterReviewerId.trim())
        : await getReviewSchedules();
      setSchedules(Array.isArray(response?.data) ? response.data : []);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Unable to load reviewer assignments.');
      setSchedules([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSchedules();
  }, []);

  const summary = useMemo(() => {
    const scheduled = schedules.filter((item) => item?.status === 'SCHEDULED').length;
    const completed = schedules.filter((item) => item?.status === 'COMPLETED').length;
    return { total: schedules.length, scheduled, completed };
  }, [schedules]);

  const handleFilter = async (event) => {
    event.preventDefault();
    await loadSchedules();
  };

  const handleCreate = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    try {
      await createReviewSchedule({
        conferenceId: form.conferenceId,
        submissionId: form.submissionId,
        reviewerUserId: form.reviewerUserId,
        scheduledAt: form.scheduledAt,
        dueAt: form.dueAt,
        adminUserId: form.adminUserId,
      });
      setMessage('Reviewer assigned successfully.');
      setForm({ conferenceId: '', submissionId: '', reviewerUserId: '', scheduledAt: '', dueAt: '', adminUserId: '' });
      await loadSchedules();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Unable to create reviewer assignment.');
    } finally {
      setSaving(false);
    }
  };

  const markCompleted = async (id) => {
    setSaving(true);
    setError('');
    setMessage('');
    try {
      await completeReviewSchedule(id);
      setMessage('Review assignment marked completed.');
      await loadSchedules();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Unable to mark assignment completed.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="card metrics-header">
        <div>
          <h2>Reviewer Assignment</h2>
          <p className="metrics-subtitle">Assign reviewers to submissions and track assignment progress.</p>
        </div>
        <div className="metrics-actions">
          <button className="btn btn-secondary" type="button" onClick={loadSchedules}>Refresh</button>
        </div>
      </div>

      <div className="metrics-grid">
        <div className="card metric-card"><p className="metric-label">Assignments</p><p className="metric-value">{summary.total}</p></div>
        <div className="card metric-card"><p className="metric-label">Scheduled</p><p className="metric-value">{summary.scheduled}</p></div>
        <div className="card metric-card"><p className="metric-label">Completed</p><p className="metric-value">{summary.completed}</p></div>
      </div>

      <div className="card">
        <form className="filters" onSubmit={handleFilter}>
          <div className="form-group">
            <label htmlFor="filterReviewer">Filter by Reviewer ID</label>
            <input
              id="filterReviewer"
              value={filterReviewerId}
              onChange={(event) => setFilterReviewerId(event.target.value)}
              placeholder="reviewer-1"
            />
          </div>
          <div className="form-group" style={{ alignSelf: 'end' }}>
            <button className="btn btn-primary" type="submit">Apply Filter</button>
          </div>
        </form>
      </div>

      {loading && <div className="card">Loading reviewer assignments...</div>}
      {!loading && error && <div className="card flagged"><strong>Could not load reviewer assignments.</strong><div>{error}</div></div>}
      {!loading && message && <div className="card issue">{message}</div>}

      <div className="analytics-two-col">
        <form className="card" onSubmit={handleCreate}>
          <h3>Assign Reviewer</h3>
          <div className="form-group">
            <label htmlFor="conferenceId">Conference ID</label>
            <input
              id="conferenceId"
              value={form.conferenceId}
              onChange={(event) => setForm({ ...form, conferenceId: event.target.value })}
              placeholder="conf-001"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="submissionId">Submission ID</label>
            <input
              id="submissionId"
              value={form.submissionId}
              onChange={(event) => setForm({ ...form, submissionId: event.target.value })}
              placeholder="submission-123"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="reviewerUserId">Reviewer User ID</label>
            <input
              id="reviewerUserId"
              value={form.reviewerUserId}
              onChange={(event) => setForm({ ...form, reviewerUserId: event.target.value })}
              placeholder="reviewer-1"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="scheduledAt">Scheduled At</label>
            <input
              id="scheduledAt"
              type="datetime-local"
              value={form.scheduledAt}
              onChange={(event) => setForm({ ...form, scheduledAt: event.target.value })}
            />
          </div>
          <div className="form-group">
            <label htmlFor="dueAt">Due At</label>
            <input
              id="dueAt"
              type="datetime-local"
              value={form.dueAt}
              onChange={(event) => setForm({ ...form, dueAt: event.target.value })}
            />
          </div>
          <div className="form-group">
            <label htmlFor="adminUserId">Assigned By Admin ID</label>
            <input
              id="adminUserId"
              value={form.adminUserId}
              onChange={(event) => setForm({ ...form, adminUserId: event.target.value })}
              placeholder="admin-1"
            />
          </div>
          <button className="btn btn-primary" type="submit" disabled={saving}>
            {saving ? 'Assigning...' : 'Assign Reviewer'}
          </button>
        </form>

        <div className="card">
          <h3>Assignments</h3>
          {schedules.length === 0 ? (
            <p>No reviewer assignments found.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Conference</th>
                  <th>Submission</th>
                  <th>Reviewer</th>
                  <th>Scheduled</th>
                  <th>Due</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {schedules.map((item) => (
                  <tr key={item.id}>
                    <td>{item.conferenceId || 'N/A'}</td>
                    <td>{item.submissionId || 'N/A'}</td>
                    <td>{item.reviewerUserId || 'N/A'}</td>
                    <td>{formatDate(item.scheduledAt)}</td>
                    <td>{formatDate(item.dueAt)}</td>
                    <td><span className={`badge badge-${item.status === 'COMPLETED' ? 'success' : 'warning'}`}>{item.status || 'N/A'}</span></td>
                    <td>
                      {item.status !== 'COMPLETED' ? (
                        <button className="btn btn-secondary" type="button" disabled={saving} onClick={() => markCompleted(item.id)}>
                          Complete
                        </button>
                      ) : (
                        <span className="badge badge-success">Done</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  );
}
