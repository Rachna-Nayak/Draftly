import { useEffect, useMemo, useState } from 'react';
import {
  getAllPapers,
  getAuditLogs,
  getMetricsDashboards,
  getProjects,
  getSubmissions,
} from '../api';

function toNumber(value) {
  const n = Number(value);
  return Number.isFinite(n) ? n : 0;
}

function fmtDate(value) {
  if (!value) return 'N/A';
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 'N/A' : parsed.toLocaleString();
}

function normalizeDateKey(value) {
  if (!value) return null;
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return null;
  const y = parsed.getFullYear();
  const m = String(parsed.getMonth() + 1).padStart(2, '0');
  const d = String(parsed.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export default function AnalyticsPage() {
  const [dashboards, setDashboards] = useState([]);
  const [logs, setLogs] = useState([]);
  const [projects, setProjects] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [papers, setPapers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadAnalytics = async () => {
    setLoading(true);
    setError('');
    try {
      const [dashboardsRes, logsRes, projectsRes, submissionsRes, papersRes] = await Promise.all([
        getMetricsDashboards(),
        getAuditLogs(),
        getProjects(),
        getSubmissions({}),
        getAllPapers(),
      ]);

      setDashboards(Array.isArray(dashboardsRes?.data) ? dashboardsRes.data : []);
      setLogs(Array.isArray(logsRes?.data) ? logsRes.data : []);
      setProjects(Array.isArray(projectsRes?.data) ? projectsRes.data : []);
      setSubmissions(Array.isArray(submissionsRes?.data) ? submissionsRes.data : []);
      setPapers(Array.isArray(papersRes?.data) ? papersRes.data : []);
    } catch (err) {
      const message =
        err?.response?.data?.message ||
        err?.message ||
        'Unable to load analytics data from backend.';
      setError(message);
      setDashboards([]);
      setLogs([]);
      setProjects([]);
      setSubmissions([]);
      setPapers([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnalytics();
  }, []);

  const summary = useMemo(() => {
    const dashboardsCount = dashboards.length;
    const dashboardProjectsTotal = dashboards.reduce((acc, item) => acc + toNumber(item.projectsCount), 0);
    const dashboardSubmissionsTotal = dashboards.reduce((acc, item) => acc + toNumber(item.submissionsCount), 0);
    const referencesTotal = dashboards.reduce((acc, item) => acc + toNumber(item.referencesCount), 0);
    const assignedReviews = dashboards.reduce((acc, item) => acc + toNumber(item.assignedReviewsCount), 0);
    const completedReviews = dashboards.reduce((acc, item) => acc + toNumber(item.completedReviewsCount), 0);
    const projectsTotal = dashboardProjectsTotal || projects.length;
    const submissionsTotal = dashboardSubmissionsTotal || submissions.length;
    const reviewCompletionPct = assignedReviews > 0
      ? Math.round((completedReviews / assignedReviews) * 100)
      : 0;
    const papersTotal = papers.length;

    return {
      dashboardsCount,
      projectsTotal,
      submissionsTotal,
      referencesTotal,
      assignedReviews,
      completedReviews,
      reviewCompletionPct,
      papersTotal,
    };
  }, [dashboards, projects.length, submissions.length, papers.length]);

  const actionBreakdown = useMemo(() => {
    const counts = logs.reduce((acc, item) => {
      const key = item?.actionType || 'unknown';
      acc[key] = (acc[key] || 0) + 1;
      return acc;
    }, {});

    return Object.entries(counts)
      .map(([actionType, count]) => ({ actionType, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 8);
  }, [logs]);

  const dailyTrend = useMemo(() => {
    const today = new Date();
    const labels = [];
    for (let i = 6; i >= 0; i -= 1) {
      const d = new Date(today);
      d.setDate(today.getDate() - i);
      labels.push(normalizeDateKey(d));
    }

    const counts = labels.reduce((acc, dateKey) => {
      acc[dateKey] = 0;
      return acc;
    }, {});

    const source = logs.length ? logs : submissions;
    source.forEach((item) => {
      const key = normalizeDateKey(item?.changedAt || item?.createdAt || item?.updatedAt);
      if (key && Object.prototype.hasOwnProperty.call(counts, key)) {
        counts[key] += 1;
      }
    });

    return labels.map((key) => ({
      dateKey: key,
      shortLabel: key ? key.slice(5) : 'N/A',
      count: counts[key] || 0,
    }));
  }, [logs, submissions]);

  const maxDailyCount = useMemo(
    () => Math.max(1, ...dailyTrend.map((point) => point.count)),
    [dailyTrend]
  );

  const recentLogs = useMemo(() => {
    return [...logs]
      .sort((a, b) => {
        const aTime = new Date(a?.changedAt || a?.createdAt || 0).getTime();
        const bTime = new Date(b?.changedAt || b?.createdAt || 0).getTime();
        return bTime - aTime;
      })
      .slice(0, 8);
  }, [logs]);

  return (
    <>
      <div className="card metrics-header">
        <div>
          <h2>Analytics</h2>
          <p className="metrics-subtitle">Analyze Draftly research workflows with live KPIs for projects, submissions, reviews, and audit activity.</p>
        </div>
        <div className="metrics-actions">
          <button className="btn btn-secondary" type="button" onClick={loadAnalytics}>
            Refresh
          </button>
        </div>
      </div>

      {loading && <div className="card">Loading analytics...</div>}

      {!loading && error && (
        <div className="card flagged">
          <strong>Could not load analytics.</strong>
          <div>{error}</div>
        </div>
      )}

      {!loading && !error && (
        <>
          <div className="metrics-grid analytics-kpis-grid">
            <div className="card metric-card">
              <p className="metric-label">Dashboards</p>
              <p className="metric-value">{summary.dashboardsCount}</p>
            </div>
            <div className="card metric-card">
              <p className="metric-label">Projects (Total)</p>
              <p className="metric-value">{summary.projectsTotal}</p>
            </div>
            <div className="card metric-card">
              <p className="metric-label">Submissions (Total)</p>
              <p className="metric-value">{summary.submissionsTotal}</p>
            </div>
            <div className="card metric-card">
              <p className="metric-label">Papers (Indexed)</p>
              <p className="metric-value">{summary.papersTotal}</p>
            </div>
            <div className="card metric-card">
              <p className="metric-label">Review Completion</p>
              <p className="metric-value">{summary.reviewCompletionPct}%</p>
            </div>
            <div className="card metric-card">
              <p className="metric-label">Audit Events</p>
              <p className="metric-value">{logs.length}</p>
            </div>
          </div>

          <div className="analytics-two-col">
            <div className="card">
              <h3>Activity Trend (Last 7 Days)</h3>
              <p className="metrics-subtitle">Number of audit events by day.</p>
              <div className="analytics-bars">
                {dailyTrend.map((point) => (
                  <div key={point.dateKey} className="analytics-bar-row">
                    <span className="analytics-bar-label">{point.shortLabel}</span>
                    <div className="analytics-bar-track">
                      <div
                        className="analytics-bar-fill"
                        style={{ width: `${Math.round((point.count / maxDailyCount) * 100)}%` }}
                      />
                    </div>
                    <span className="analytics-bar-value">{point.count}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="card">
              <h3>Top Action Types</h3>
              <p className="metrics-subtitle">Most frequent actions captured in logs.</p>
              {actionBreakdown.length === 0 ? (
                <p>No action data available.</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Action</th>
                      <th>Count</th>
                    </tr>
                  </thead>
                  <tbody>
                    {actionBreakdown.map((item) => (
                      <tr key={item.actionType}>
                        <td>{item.actionType}</td>
                        <td>{item.count}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          <div className="card">
            <h3>Recent Activity</h3>
            <p className="metrics-subtitle">Latest backend audit events.</p>
            {recentLogs.length === 0 ? (
              <p>No recent logs available.</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>When</th>
                    <th>Action</th>
                    <th>Entity</th>
                    <th>User</th>
                    <th>Message</th>
                  </tr>
                </thead>
                <tbody>
                  {recentLogs.map((item) => (
                    <tr key={item.id}>
                      <td>{fmtDate(item.changedAt || item.createdAt)}</td>
                      <td>{item.actionType || 'N/A'}</td>
                      <td>{item.entityType || 'N/A'}</td>
                      <td>{item.userId || 'N/A'}</td>
                      <td>{item.message || item.changeSummary || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </>
  );
}
