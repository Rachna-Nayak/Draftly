import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import * as api from '../api';
import '../components/Layout.css';

const MyReviewsPage = () => {
  const [pendingReviews, setPendingReviews] = useState([]);
  const [completedReviews, setCompletedReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('pending');
  const navigate = useNavigate();

  useEffect(() => {
    loadReviews();
  }, []);

  const loadReviews = async () => {
    try {
      setLoading(true);
      const response = await api.getMyPendingReviews();
      const reviews = Array.isArray(response?.data) ? response.data : [];

      const now = new Date();
      const pending = reviews.filter(r => !r.completed && (!r.dueDate || new Date(r.dueDate) > now));
      const completed = reviews.filter(r => r.completed);

      setPendingReviews(pending);
      setCompletedReviews(completed);
      setError('');
    } catch (err) {
      setError('Failed to load reviews: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const getDaysUntilDue = (dueDate) => {
    if (!dueDate) return null;
    const now = new Date();
    const due = new Date(dueDate);
    const days = Math.ceil((due - now) / (1000 * 60 * 60 * 24));
    return days;
  };

  const getUrgencyBadgeColor = (daysUntilDue) => {
    if (daysUntilDue === null) return 'badge-info';
    if (daysUntilDue < 0) return 'badge-danger';
    if (daysUntilDue <= 3) return 'badge-warning';
    return 'badge-info';
  };

  const getUrgencyText = (daysUntilDue) => {
    if (daysUntilDue === null) return 'No deadline';
    if (daysUntilDue < 0) return `Overdue by ${Math.abs(daysUntilDue)} day(s)`;
    if (daysUntilDue === 0) return 'Due today';
    return `Due in ${daysUntilDue} day(s)`;
  };

  const handleReviewClick = (submissionId) => {
    navigate(`/reviews/${submissionId}`);
  };

  if (loading) {
    return <div className="container"><div className="card">Loading reviews...</div></div>;
  }

  return (
    <div className="container">
      <div className="card">
        <h2>My Reviews</h2>

        {error && <div className="card flagged">{error}</div>}

        <div className="metrics-grid">
          <div className="metric-card">
            <div className="metric-value">{pendingReviews.length}</div>
            <div className="metric-label">Pending Reviews</div>
          </div>
          <div className="metric-card">
            <div className="metric-value">{completedReviews.length}</div>
            <div className="metric-label">Completed Reviews</div>
          </div>
        </div>

        <div className="tabs">
          <button
            className={`tab ${activeTab === 'pending' ? 'active' : ''}`}
            onClick={() => setActiveTab('pending')}
          >
            Pending ({pendingReviews.length})
          </button>
          <button
            className={`tab ${activeTab === 'completed' ? 'active' : ''}`}
            onClick={() => setActiveTab('completed')}
          >
            Completed ({completedReviews.length})
          </button>
        </div>

        {activeTab === 'pending' && (
          <div>
            {pendingReviews.length === 0 ? (
              <div className="card issue">No pending reviews at this time!</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Conference</th>
                    <th>Track</th>
                    <th>Due Date</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingReviews.map((review) => {
                    const daysUntilDue = getDaysUntilDue(review.dueDate);
                    return (
                      <tr key={review.submissionId}>
                        <td><strong>{review.title}</strong></td>
                        <td>{review.conference}</td>
                        <td>{review.track}</td>
                        <td>
                          <span className={`badge ${getUrgencyBadgeColor(daysUntilDue)}`}>
                            {getUrgencyText(daysUntilDue)}
                          </span>
                        </td>
                        <td>
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => handleReviewClick(review.submissionId)}
                          >
                            Review
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'completed' && (
          <div>
            {completedReviews.length === 0 ? (
              <div className="card issue">No completed reviews yet.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Conference</th>
                    <th>Decision</th>
                    <th>Completed</th>
                  </tr>
                </thead>
                <tbody>
                  {completedReviews.map((review) => (
                    <tr key={review.submissionId}>
                      <td><strong>{review.title}</strong></td>
                      <td>{review.conference}</td>
                      <td><span className="badge badge-success">Completed</span></td>
                      <td>{new Date(review.completedAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyReviewsPage;
