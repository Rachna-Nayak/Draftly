import { useEffect, useMemo, useState } from 'react';
import {
  completeReviewSchedule,
  createReview,
  createReviewSchedule,
  getReviewSchedules,
  getReviews,
  getReviewsByReviewer,
  getReviewSchedulesByReviewer,
} from '../api';

const reviewDecisionOptions = ['ACCEPT', 'REVISION', 'REJECT'];

function formatDate(value) {
  if (!value) return 'N/A';
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 'N/A' : parsed.toLocaleString();
}

function Badge({ children, tone = 'info' }) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}

export default function ReviewQueue() {
  const [schedules, setSchedules] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [filterReviewerId, setFilterReviewerId] = useState('');
  const [scheduleForm, setScheduleForm] = useState({
    conferenceId: '',
    submissionId: '',
    reviewerUserId: '',
    scheduledAt: '',
    dueAt: '',
    adminUserId: '',
  });
  const [reviewForm, setReviewForm] = useState({
    submissionId: '',
    reviewerId: '',
    rating: 5,
    comments: '',
    decision: 'ACCEPT',
  });
  const [message, setMessage] = useState('');

  const loadQueue = async () => {
    setLoading(true);
    setError('');
    try {
      const [schedulesRes, reviewsRes] = await Promise.all([
        filterReviewerId ? getReviewSchedulesByReviewer(filterReviewerId) : getReviewSchedules(),
        filterReviewerId ? getReviewsByReviewer(filterReviewerId) : getReviews(),
      ]);
      setSchedules(Array.isArray(schedulesRes?.data) ? schedulesRes.data : []);
      setReviews(Array.isArray(reviewsRes?.data) ? reviewsRes.data : []);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Unable to load review queue.');
      setSchedules([]);
      setReviews([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadQueue();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const summary = useMemo(() => {
    const scheduled = schedules.filter((item) => item?.status === 'SCHEDULED').length;
    const completed = schedules.filter((item) => item?.status === 'COMPLETED').length;
    const underReview = reviews.filter((item) => item?.decision === 'REVISION').length;
    return { scheduled, completed, underReview, totalReviews: reviews.length };
  }, [schedules, reviews]);

  const handleLoadByReviewer = async (event) => {
    event.preventDefault();
    await loadQueue();
  };

  const submitSchedule = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage('');
    try {
      const payload = {
        conferenceId: scheduleForm.conferenceId,
        submissionId: scheduleForm.submissionId,
        reviewerUserId: scheduleForm.reviewerUserId,
        scheduledAt: scheduleForm.scheduledAt,
        dueAt: scheduleForm.dueAt,
        adminUserId: scheduleForm.adminUserId,
      };
      await createReviewSchedule(payload);
      setScheduleForm({ conferenceId: '', submissionId: '', reviewerUserId: '', scheduledAt: '', dueAt: '', adminUserId: '' });
      setMessage('Review schedule created successfully.');
      await loadQueue();
    } catch (err) {
      setMessage(err?.response?.data?.message || err?.message || 'Failed to create review schedule.');
    } finally {
      setSaving(false);
    }
  };

  const submitReview = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage('');
    try {
      await createReview({
        submissionId: reviewForm.submissionId,
        reviewerId: reviewForm.reviewerId,
        rating: Number(reviewForm.rating),
        comments: reviewForm.comments,
        decision: reviewForm.decision,
      });
      setReviewForm({ submissionId: '', reviewerId: '', rating: 5, comments: '', decision: 'ACCEPT' });
      setMessage('Review submitted successfully.');
      await loadQueue();
    } catch (err) {
      setMessage(err?.response?.data?.message || err?.message || 'Failed to submit review.');
    } finally {
      setSaving(false);
    }
  };

  const markCompleted = async (id) => {
    setSaving(true);
    setMessage('');
    try {
      await completeReviewSchedule(id);
      setMessage('Review schedule marked completed.');
      await loadQueue();
    } catch (err) {
      setMessage(err?.response?.data?.message || err?.message || 'Failed to complete review schedule.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="card metrics-header">
        <div>
          <h2>Review Queue</h2>
          <p className="metrics-subtitle">Track review assignments, reviews submitted, and schedule status in one place.</p>
        </div>
        <div className="metrics-actions">
          <button className="btn btn-secondary" type="button" onClick={loadQueue}>
            Refresh
          </button>
        </div>
      </div>

      <div className="metrics-grid">
        <div className="card metric-card"><p className="metric-label">Scheduled</p><p className="metric-value">{summary.scheduled}</p></div>
        <div className="card metric-card"><p className="metric-label">Completed</p><p className="metric-value">{summary.completed}</p></div>
        <div className="card metric-card"><p className="metric-label">Reviews</p><p className="metric-value">{summary.totalReviews}</p></div>
        <div className="card metric-card"><p className="metric-label">Revision Decisions</p><p className="metric-value">{summary.underReview}</p></div>
      </div>

      <div className="card">
        <form className="filters" onSubmit={handleLoadByReviewer}>
          <div className="form-group">
            <label htmlFor="filterReviewerId">Filter by Reviewer User ID</label>
            <input
              id="filterReviewerId"
              value={filterReviewerId}
              onChange={(event) => setFilterReviewerId(event.target.value)}
              placeholder="reviewer1"
            />
          </div>
          <div className="form-group" style={{ alignSelf: 'end' }}>
            <button className="btn btn-primary" type="submit">Apply Filter</button>
          </div>
        </form>
      </div>

      {loading && <div className="card">Loading review queue...</div>}

      {!loading && error && (
        <div className="card flagged">
          <strong>Could not load review data.</strong>
          <div>{error}</div>
        </div>
      )}

      {!loading && message && <div className="card issue">{message}</div>}

      <div className="analytics-two-col">
        <form className="card" onSubmit={submitSchedule}>
          <h3>Create Review Schedule</h3>
          <div className="form-group">
            <label htmlFor="conferenceId">Conference ID</label>
            <input
              id="conferenceId"
              value={scheduleForm.conferenceId}
              onChange={(event) => setScheduleForm({ ...scheduleForm, conferenceId: event.target.value })}
              placeholder="conf-001"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="scheduleSubmissionId">Submission ID</label>
            <input
              id="scheduleSubmissionId"
              value={scheduleForm.submissionId}
              onChange={(event) => setScheduleForm({ ...scheduleForm, submissionId: event.target.value })}
              placeholder="submission-123"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="scheduleReviewerUserId">Reviewer User ID</label>
            <input
              id="scheduleReviewerUserId"
              value={scheduleForm.reviewerUserId}
              onChange={(event) => setScheduleForm({ ...scheduleForm, reviewerUserId: event.target.value })}
              placeholder="reviewer1"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="scheduledAt">Scheduled At</label>
            <input
              id="scheduledAt"
              type="datetime-local"
              value={scheduleForm.scheduledAt}
              onChange={(event) => setScheduleForm({ ...scheduleForm, scheduledAt: event.target.value })}
            />
          </div>
          <div className="form-group">
            <label htmlFor="dueAt">Due At</label>
            <input
              id="dueAt"
              type="datetime-local"
              value={scheduleForm.dueAt}
              onChange={(event) => setScheduleForm({ ...scheduleForm, dueAt: event.target.value })}
            />
          </div>
          <div className="form-group">
            <label htmlFor="scheduleAdminUserId">Admin User ID</label>
            <input
              id="scheduleAdminUserId"
              value={scheduleForm.adminUserId}
              onChange={(event) => setScheduleForm({ ...scheduleForm, adminUserId: event.target.value })}
              placeholder="admin1"
            />
          </div>
          <button className="btn btn-primary" type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Create Schedule'}
          </button>

          <div style={{ marginTop: '1rem' }}>
            <h3>Submit Review</h3>
            <div className="form-group">
              <label htmlFor="reviewSubmissionId">Submission ID</label>
              <input
                id="reviewSubmissionId"
                value={reviewForm.submissionId}
                onChange={(event) => setReviewForm({ ...reviewForm, submissionId: event.target.value })}
                placeholder="submission-123"
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="reviewerId">Reviewer ID</label>
              <input
                id="reviewerId"
                value={reviewForm.reviewerId}
                onChange={(event) => setReviewForm({ ...reviewForm, reviewerId: event.target.value })}
                placeholder="reviewer1"
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="rating">Rating</label>
              <input
                id="rating"
                type="number"
                min="1"
                max="10"
                value={reviewForm.rating}
                onChange={(event) => setReviewForm({ ...reviewForm, rating: event.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="decision">Decision</label>
              <select
                id="decision"
                value={reviewForm.decision}
                onChange={(event) => setReviewForm({ ...reviewForm, decision: event.target.value })}
              >
                {reviewDecisionOptions.map((option) => (
                  <option key={option} value={option}>{option}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="comments">Comments</label>
              <textarea
                id="comments"
                value={reviewForm.comments}
                onChange={(event) => setReviewForm({ ...reviewForm, comments: event.target.value })}
                placeholder="Write review comments..."
                required
              />
            </div>
            <button className="btn btn-success" type="submit" disabled={saving}>
              {saving ? 'Submitting...' : 'Submit Review'}
            </button>
          </div>
        </form>

        <div className="card">
          <h3>Scheduled Reviews</h3>
          {schedules.length === 0 ? (
            <p>No review schedules found.</p>
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
                    <td><Badge tone={item.status === 'COMPLETED' ? 'success' : 'warning'}>{item.status || 'N/A'}</Badge></td>
                    <td>
                      {item.status !== 'COMPLETED' ? (
                        <button className="btn btn-secondary" type="button" disabled={saving} onClick={() => markCompleted(item.id)}>
                          Complete
                        </button>
                      ) : (
                        <Badge tone="success">Done</Badge>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      <div className="card">
        <h3>Submitted Reviews</h3>
        {reviews.length === 0 ? (
          <p>No reviews found.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>When</th>
                <th>Submission</th>
                <th>Reviewer</th>
                <th>Rating</th>
                <th>Decision</th>
                <th>Comments</th>
              </tr>
            </thead>
            <tbody>
              {reviews.map((item) => (
                <tr key={item.id}>
                  <td>{formatDate(item.reviewDate)}</td>
                  <td>{item.submissionId || 'N/A'}</td>
                  <td>{item.reviewerId || item.reviewerUserId || 'N/A'}</td>
                  <td>{item.rating ?? 'N/A'}</td>
                  <td>{item.decision || 'N/A'}</td>
                  <td>{item.comments || item.summary || 'N/A'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}
