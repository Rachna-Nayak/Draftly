import { useEffect, useMemo, useState } from 'react';
import { getMetricsDashboards } from '../api';

function safeNumber(value) {
  return Number.isFinite(Number(value)) ? Number(value) : 0;
}

function StatCard({ label, value }) {
  return (
    <div className="card metric-card">
      <p className="metric-label">{label}</p>
      <p className="metric-value">{safeNumber(value)}</p>
    </div>
  );
}

export default function MetricsDashboard() {
  const [dashboards, setDashboards] = useState([]);
  const [selectedId, setSelectedId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDashboards = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await getMetricsDashboards();
      const list = Array.isArray(response?.data) ? response.data : [];
      setDashboards(list);

      if (!selectedId && list.length > 0) {
        setSelectedId(list[0].id);
      }
    } catch (err) {
      const message =
        err?.response?.data?.message ||
        err?.message ||
        'Unable to load dashboard metrics.';
      setError(message);
      setDashboards([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboards();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const selectedDashboard = useMemo(() => {
    if (!dashboards.length) return null;
    return dashboards.find((item) => item.id === selectedId) || dashboards[0];
  }, [dashboards, selectedId]);

  const reviewCompletionPercent = useMemo(() => {
    if (!selectedDashboard) return 0;
    const completed = safeNumber(selectedDashboard.completedReviewsCount);
    const assigned = safeNumber(selectedDashboard.assignedReviewsCount);
    if (assigned <= 0) return 0;
    return Math.min(100, Math.round((completed / assigned) * 100));
  }, [selectedDashboard]);

  return (
    <>
      <div className="card metrics-header">
        <div>
          <h2>Metrics Dashboard</h2>
          <p className="metrics-subtitle">
            Overview KPIs for projects, submissions, reviews, and platform usage.
          </p>
        </div>

        <div className="metrics-actions">
          <button className="btn btn-secondary" type="button" onClick={loadDashboards}>
            Refresh
          </button>
        </div>
      </div>

      {loading && <div className="card">Loading dashboard metrics...</div>}

      {!loading && error && (
        <div className="card flagged">
          <strong>Could not load metrics.</strong>
          <div>{error}</div>
        </div>
      )}

      {!loading && !error && dashboards.length === 0 && (
        <div className="card">
          No metrics dashboards found yet. Create one from the backend or seed sample data.
        </div>
      )}

      {!loading && !error && selectedDashboard && (
        <>
          <div className="card metrics-scope-row">
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="scopeSelect">Dashboard Scope</label>
              <select
                id="scopeSelect"
                value={selectedDashboard.id}
                onChange={(event) => setSelectedId(event.target.value)}
              >
                {dashboards.map((item) => (
                  <option key={item.id} value={item.id}>
                    {(item.scopeType || 'unknown').toUpperCase()} · {item.scopeId || 'N/A'}
                  </option>
                ))}
              </select>
            </div>
            <div className="metrics-updated-at">
              Last updated:{' '}
              {selectedDashboard.lastUpdatedAt
                ? new Date(selectedDashboard.lastUpdatedAt).toLocaleString()
                : 'N/A'}
            </div>
          </div>

          <div className="metrics-grid">
            <StatCard label="Projects" value={selectedDashboard.projectsCount} />
            <StatCard label="Submissions" value={selectedDashboard.submissionsCount} />
            <StatCard label="References" value={selectedDashboard.referencesCount} />
            <StatCard label="Assigned Reviews" value={selectedDashboard.assignedReviewsCount} />
            <StatCard label="Completed Reviews" value={selectedDashboard.completedReviewsCount} />
            <StatCard label="Pending Reviews" value={selectedDashboard.pendingReviewsCount} />
            <StatCard label="Users" value={selectedDashboard.usersCount} />
            <StatCard
              label="Submissions Per Track"
              value={selectedDashboard.submissionsPerTrackCount}
            />
            <StatCard label="Review Stats" value={selectedDashboard.reviewStatsCount} />
          </div>

          <div className="card">
            <h3>Review Completion</h3>
            <p className="metrics-subtitle" style={{ marginBottom: '0.6rem' }}>
              Completed reviews as a share of assigned reviews.
            </p>
            <div className="score-bar">
              <div
                className="score-fill"
                style={{ width: `${reviewCompletionPercent}%` }}
                aria-label={`Review completion ${reviewCompletionPercent}%`}
              />
            </div>
            <strong>{reviewCompletionPercent}% complete</strong>
          </div>
        </>
      )}
    </>
  );
}
