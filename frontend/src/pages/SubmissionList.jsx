import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  getCurrentUserId,
  getCurrentUser,
  getCurrentUserRole,
} from '../authStorage';
import {
  getSubmission,
  getSubmissionFeedback,
  getSubmissionReviews,
  getSubmissionVersions,
  getSubmissions,
  publishSubmission,
  submitSubmission,
} from '../api';

function formatDate(value) {
  if (!value) return 'N/A';
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? 'N/A' : parsed.toLocaleString();
}

function statusTone(status) {
  if (!status) return 'info';
  if (['PUBLISHED', 'ACCEPTED'].includes(status)) return 'success';
  if (['SUBMITTED', 'UNDER_REVIEW'].includes(status)) return 'warning';
  if (['REVISION_REQUIRED', 'REJECTED'].includes(status)) return 'danger';
  return 'info';
}

export default function SubmissionList() {
  const currentUser = getCurrentUser();
  const currentUserId = getCurrentUserId();
  const currentRole = getCurrentUserRole();
  const isAdmin = currentRole === 'ADMIN';

  const [queryType, setQueryType] = useState(isAdmin ? 'all' : 'authorId');
  const [authorId, setAuthorId] = useState(currentUserId || '');
  const [projectId, setProjectId] = useState('');
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedSubmissionId, setSelectedSubmissionId] = useState('');
  const [detailLoading, setDetailLoading] = useState(false);
  const [details, setDetails] = useState({ submission: null, versions: [], reviews: [], feedback: [] });
  const [actionMessage, setActionMessage] = useState('');
  const [publishForm, setPublishForm] = useState({ doi: '', journal: '', publicationYear: new Date().getFullYear() });

  const loadSubmissions = async () => {
    setLoading(true);
    setError('');
    try {
      const params = queryType === 'projectId'
        ? { projectId: projectId.trim() }
        : queryType === 'all'
          ? {}
          : { authorId: authorId.trim() || currentUserId || 'default' };
      const response = await getSubmissions(params);
      const list = Array.isArray(response?.data) ? response.data : [];
      setSubmissions(list);
      setSelectedSubmissionId('');
      setDetails({ submission: null, versions: [], reviews: [], feedback: [] });
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Unable to load submissions.');
      setSubmissions([]);
      setSelectedSubmissionId('');
      setDetails({ submission: null, versions: [], reviews: [], feedback: [] });
    } finally {
      setLoading(false);
    }
  };

  const loadSubmissionDetails = async (submissionId) => {
    if (!submissionId) return;
    setDetailLoading(true);
    setActionMessage('');
    try {
      const [submissionRes, versionsRes, reviewsRes, feedbackRes] = await Promise.all([
        getSubmission(submissionId),
        getSubmissionVersions(submissionId),
        getSubmissionReviews(submissionId),
        getSubmissionFeedback(submissionId),
      ]);

      setDetails({
        submission: submissionRes?.data || null,
        versions: Array.isArray(versionsRes?.data) ? versionsRes.data : [],
        reviews: Array.isArray(reviewsRes?.data) ? reviewsRes.data : [],
        feedback: Array.isArray(feedbackRes?.data) ? feedbackRes.data : [],
      });
      setPublishForm({
        doi: '',
        journal: '',
        publicationYear: new Date().getFullYear(),
      });
    } catch (err) {
      setActionMessage(err?.response?.data?.message || err?.message || 'Unable to load submission details.');
      setDetails({ submission: null, versions: [], reviews: [], feedback: [] });
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    loadSubmissions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (selectedSubmissionId) {
      loadSubmissionDetails(selectedSubmissionId);
    }
  }, [selectedSubmissionId]);

  const summary = useMemo(() => {
    const total = submissions.length;
    const submitted = submissions.filter((item) => item?.status === 'SUBMITTED').length;
    const underReview = submissions.filter((item) => item?.status === 'UNDER_REVIEW').length;
    const published = submissions.filter((item) => item?.status === 'PUBLISHED').length;
    return { total, submitted, underReview, published };
  }, [submissions]);

  const handleSearch = async (event) => {
    event.preventDefault();
    await loadSubmissions();
  };

  const handleSubmitPaper = async (submissionId) => {
    setActionMessage('');
    try {
      await submitSubmission(submissionId);
      setActionMessage('Submission moved to SUBMITTED.');
      await loadSubmissions();
      await loadSubmissionDetails(submissionId);
    } catch (err) {
      setActionMessage(err?.response?.data?.message || err?.message || 'Unable to submit submission.');
    }
  };

  const handlePublish = async (event) => {
    event.preventDefault();
    if (!selectedSubmissionId) return;

    setActionMessage('');
    try {
      await publishSubmission(selectedSubmissionId, {
        doi: publishForm.doi,
        journal: publishForm.journal,
        publicationYear: Number(publishForm.publicationYear),
      });
      setActionMessage('Submission published successfully.');
      await loadSubmissions();
      await loadSubmissionDetails(selectedSubmissionId);
    } catch (err) {
      setActionMessage(err?.response?.data?.message || err?.message || 'Unable to publish submission.');
    }
  };

  const selectedSubmission = details.submission || submissions.find((item) => item.id === selectedSubmissionId) || null;

  return (
    <>
      <div className="card metrics-header">
        <div>
          <h2>Submission List</h2>
          <p className="metrics-subtitle">Manage research submissions, review progress, and publishing status.</p>
        </div>
        <div className="metrics-actions">
          <button className="btn btn-secondary" type="button" onClick={loadSubmissions}>Refresh</button>
          <Link to="/submissions/new" className="btn btn-primary">+ New Submission</Link>
        </div>
      </div>

      <div className="metrics-grid">
        <div className="card metric-card"><p className="metric-label">Total</p><p className="metric-value">{summary.total}</p></div>
        <div className="card metric-card"><p className="metric-label">Submitted</p><p className="metric-value">{summary.submitted}</p></div>
        <div className="card metric-card"><p className="metric-label">Under Review</p><p className="metric-value">{summary.underReview}</p></div>
        <div className="card metric-card"><p className="metric-label">Published</p><p className="metric-value">{summary.published}</p></div>
      </div>

      <div className="card">
        <form className="filters" onSubmit={handleSearch}>
          <div className="form-group">
            <label htmlFor="queryType">Search By</label>
            <select id="queryType" value={queryType} onChange={(event) => setQueryType(event.target.value)}>
              {isAdmin && <option value="all">All</option>}
              <option value="authorId">Author</option>
              <option value="projectId">Project</option>
            </select>
          </div>
          {queryType === 'authorId' ? (
            <div className="form-group">
              <label htmlFor="authorId">Author ID</label>
              <input
                id="authorId"
                value={authorId}
                onChange={(event) => setAuthorId(event.target.value)}
                placeholder={currentUser?.id || currentUserId || 'author-1'}
              />
            </div>
          ) : queryType === 'projectId' ? (
            <div className="form-group">
              <label htmlFor="projectId">Project ID</label>
              <input
                id="projectId"
                value={projectId}
                onChange={(event) => setProjectId(event.target.value)}
                placeholder="project-1"
              />
            </div>
          ) : (
            <div className="form-group">
              <label>Scope</label>
              <input value="All submissions" disabled readOnly />
            </div>
          )}
          <div className="form-group" style={{ alignSelf: 'end' }}>
            <button className="btn btn-primary" type="submit">Load Submissions</button>
          </div>
        </form>
      </div>

      {loading && <div className="card">Loading submissions...</div>}

      {!loading && error && (
        <div className="card flagged">
          <strong>Could not load submissions.</strong>
          <div>{error}</div>
        </div>
      )}

      {!loading && actionMessage && <div className="card issue">{actionMessage}</div>}

      <div className="analytics-two-col">
        <div className="card">
          <h3>All Submissions</h3>
          {submissions.length === 0 ? (
            <p>No submissions found for the selected filter.</p>
          ) : (
            <div className="submission-table-wrap">
              <table className="submission-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Conference</th>
                    <th>Track</th>
                    <th>Status</th>
                    <th>Updated</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {submissions.map((submission) => (
                    <tr
                      key={submission.id}
                      className={`submission-row ${submission.id === selectedSubmissionId ? 'is-selected' : ''}`}
                    >
                      <td>
                        <div className="submission-title">{submission.title || 'Untitled'}</div>
                      </td>
                      <td>{submission.conferenceName || 'N/A'}</td>
                      <td>{submission.track || 'N/A'}</td>
                      <td>
                        <span className={`badge badge-${statusTone(submission.status)}`}>
                          {submission.status || 'N/A'}
                        </span>
                      </td>
                      <td>{formatDate(submission.updatedAt || submission.createdAt)}</td>
                      <td>
                        <button
                          className="btn btn-secondary"
                          type="button"
                          onClick={() => setSelectedSubmissionId(submission.id)}
                        >
                          {submission.id === selectedSubmissionId ? 'Viewing' : 'View'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="card submission-details-card">
          <h3>Submission Details</h3>
          {detailLoading && <p>Loading details...</p>}
          {!detailLoading && !selectedSubmission && <p>Select a submission to see its versions, reviews, and feedback.</p>}

          {!detailLoading && selectedSubmission && (
            <div className="page-stack">
              <div>
                <p><strong>Project:</strong> {selectedSubmission.projectId || 'N/A'}</p>
                <p><strong>Author:</strong> {selectedSubmission.authorId || 'N/A'}</p>
                <p><strong>Conference:</strong> {selectedSubmission.conferenceName || 'N/A'}</p>
                <p><strong>Track:</strong> {selectedSubmission.track || 'N/A'}</p>
                <p><strong>Docx Path:</strong> {selectedSubmission.docxPath || 'N/A'}</p>
                <p><strong>Reference Count:</strong> {selectedSubmission.referenceIds?.length || 0}</p>
                <p><strong>Created:</strong> {formatDate(selectedSubmission.createdAt)}</p>
              </div>

              <div>
                <h4>Versions</h4>
                {details.versions.length === 0 ? <p>No versions uploaded yet.</p> : details.versions.map((version) => (
                  <div key={version.id} className="issue" style={{ marginBottom: '0.5rem' }}>
                    <strong>Version {version.versionNumber}</strong>
                    <div>{version.filePath || 'N/A'}</div>
                    <div>{version.notes || 'No notes provided.'}</div>
                  </div>
                ))}
              </div>

              <div>
                <h4>Reviews</h4>
                {details.reviews.length === 0 ? <p>No reviews recorded yet.</p> : details.reviews.map((review) => (
                  <div key={review.id} className="feedback-item">
                    <strong>{review.reviewerId || 'Reviewer'}</strong> · <span>{review.decision || 'N/A'}</span>
                    <div>Rating: {review.rating ?? 'N/A'}</div>
                    <div>{review.comments || 'No comments.'}</div>
                    <div style={{ fontSize: '0.8rem', color: '#6b7d8f' }}>{formatDate(review.reviewDate)}</div>
                  </div>
                ))}
              </div>

              <div>
                <h4>Feedback</h4>
                {details.feedback.length === 0 ? <p>No feedback yet.</p> : details.feedback.map((item) => (
                  <div key={item.id} className="feedback-item">
                    <strong>{item.reviewerId || 'Reviewer'}</strong>
                    <div>{item.message || 'No message.'}</div>
                  </div>
                ))}
              </div>

              <div className="actions" style={{ flexWrap: 'wrap' }}>
                {selectedSubmission.status !== 'SUBMITTED' && selectedSubmission.status !== 'UNDER_REVIEW' && (
                  <button className="btn btn-success" type="button" onClick={() => handleSubmitPaper(selectedSubmission.id)}>
                    Submit for Review
                  </button>
                )}
                {(selectedSubmission.status === 'ACCEPTED') && (
                  <form onSubmit={handlePublish} style={{ display: 'grid', gap: '0.75rem', width: '100%' }}>
                    <h4>Publish Submission</h4>
                    <div className="form-group">
                      <label htmlFor="doi">DOI</label>
                      <input id="doi" value={publishForm.doi} onChange={(event) => setPublishForm({ ...publishForm, doi: event.target.value })} placeholder="10.1234/draftly.001" />
                    </div>
                    <div className="form-group">
                      <label htmlFor="journal">Journal</label>
                      <input id="journal" value={publishForm.journal} onChange={(event) => setPublishForm({ ...publishForm, journal: event.target.value })} placeholder="Draftly Journal" />
                    </div>
                    <div className="form-group">
                      <label htmlFor="publicationYear">Publication Year</label>
                      <input id="publicationYear" type="number" value={publishForm.publicationYear} onChange={(event) => setPublishForm({ ...publishForm, publicationYear: event.target.value })} />
                    </div>
                    <button className="btn btn-primary" type="submit">Publish</button>
                  </form>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <Link to="/projects" className="btn btn-secondary">← Back to Projects</Link>
    </>
  );
}
