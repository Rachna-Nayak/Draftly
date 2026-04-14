import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as api from '../api';
import '../components/Layout.css';

const ReviewDetailPage = () => {
  const { submissionId } = useParams();
  const navigate = useNavigate();
  const [submission, setSubmission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const [reviewForm, setReviewForm] = useState({
    submissionId: submissionId,
    reviewerId: localStorage.getItem('draftly.auth')
      ? JSON.parse(localStorage.getItem('draftly.auth')).userId
      : '',
    reviewDecision: 'ACCEPT',
    comments: '',
    technicalQuality: '',
    clarity: '',
    originality: '',
    significance: '',
    confidence: 'MEDIUM',
  });

  useEffect(() => {
    loadSubmissionContext();
  }, [submissionId]);

  const loadSubmissionContext = async () => {
    try {
      setLoading(true);
      const response = await api.getSubmissionReviewContext(submissionId);
      setSubmission(response?.data ?? null);
      setError('');
    } catch (err) {
      setError('Failed to load submission: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setReviewForm((prev) => ({
      ...prev,
      [name]: value === '' ? '' : isNaN(value) ? value : parseInt(value),
    }));
  };

  const validateForm = () => {
    if (!reviewForm.comments || reviewForm.comments.length < 50) {
      setError('Comments must be at least 50 characters');
      return false;
    }
    if (!reviewForm.reviewDecision) {
      setError('Please select a decision');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setSubmitting(true);
      setError('');

      const payload = {
        submissionId: reviewForm.submissionId,
        reviewerId: reviewForm.reviewerId,
        reviewDecision: reviewForm.reviewDecision,
        comments: reviewForm.comments,
        confidence: reviewForm.confidence,
      };

      if (reviewForm.technicalQuality) payload.technicalQuality = reviewForm.technicalQuality;
      if (reviewForm.clarity) payload.clarity = reviewForm.clarity;
      if (reviewForm.originality) payload.originality = reviewForm.originality;
      if (reviewForm.significance) payload.significance = reviewForm.significance;

      await api.submitReview(payload);

      // Show success and redirect
      alert('Review submitted successfully!');
      navigate('/reviews/pending');
    } catch (err) {
      setError('Failed to submit review: ' + err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="container"><div className="card">Loading submission...</div></div>;
  }

  if (!submission) {
    return <div className="container"><div className="card flagged">Submission not found</div></div>;
  }

  const charCount = reviewForm.comments.length;
  const isCommentValid = charCount >= 50;

  return (
    <div className="container">
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr 1fr', gap: '20px' }}>
        {/* Left Panel: Submission Context */}
        <div className="card">
          <h3>Submission Details</h3>
          <div className="form-group">
            <label>Title</label>
            <p><strong>{submission.title}</strong></p>
          </div>
          <div className="form-group">
            <label>Conference</label>
            <p>{submission.conferenceName}</p>
          </div>
          <div className="form-group">
            <label>Track</label>
            <p>{submission.track}</p>
          </div>
          <div className="form-group">
            <label>Submission Date</label>
            <p>{new Date(submission.submissionDate).toLocaleDateString()}</p>
          </div>
          {submission.keywords && submission.keywords.length > 0 && (
            <div className="form-group">
              <label>Keywords</label>
              <div>
                {submission.keywords.map((keyword, idx) => (
                  <span key={idx} className="badge badge-info">
                    {keyword}
                  </span>
                ))}
              </div>
            </div>
          )}
          <div className="form-group">
            <label>Abstract</label>
            <p className="text-muted">{submission.abstractText || 'No abstract provided'}</p>
          </div>
          <div className="form-group">
            <label>References</label>
            <p>{submission.referenceCount} references</p>
          </div>
          {submission.docxPath && (
            <div className="form-group">
              <a href={`/api/files/${submission.docxPath}`} className="btn btn-secondary btn-sm" target="_blank" rel="noreferrer">
                Download Paper
              </a>
            </div>
          )}
        </div>

        {/* Center Panel: Review Form */}
        <div className="card">
          <h3>Submit Your Review</h3>

          {error && <div className="card flagged">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="reviewDecision">Decision *</label>
              <select
                id="reviewDecision"
                name="reviewDecision"
                value={reviewForm.reviewDecision}
                onChange={handleFormChange}
                required
              >
                <option value="">-- Select Decision --</option>
                <option value="STRONG_ACCEPT">Strong Accept - Excellent paper</option>
                <option value="ACCEPT">Accept - Good paper</option>
                <option value="BORDERLINE">Borderline - Needs improvement</option>
                <option value="REJECT">Reject - Below standards</option>
                <option value="STRONG_REJECT">Strong Reject - Poor quality</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="comments">Comments *</label>
              <textarea
                id="comments"
                name="comments"
                value={reviewForm.comments}
                onChange={handleFormChange}
                placeholder="Enter detailed review comments (minimum 50 characters)"
                rows="8"
                required
              />
              <small className={isCommentValid ? 'text-success' : 'text-danger'}>
                {charCount}/50 characters minimum
              </small>
            </div>

            <div className="form-group">
              <label htmlFor="confidence">Confidence Level</label>
              <select
                id="confidence"
                name="confidence"
                value={reviewForm.confidence}
                onChange={handleFormChange}
              >
                <option value="HIGH">High - Very confident</option>
                <option value="MEDIUM">Medium - Reasonably confident</option>
                <option value="LOW">Low - Somewhat uncertain</option>
              </select>
            </div>

            <fieldset className="form-group">
              <legend>Optional Category Ratings (1-5)</legend>

              <div className="form-group">
                <label htmlFor="technicalQuality">Technical Quality</label>
                <input
                  type="number"
                  id="technicalQuality"
                  name="technicalQuality"
                  min="1"
                  max="5"
                  value={reviewForm.technicalQuality}
                  onChange={handleFormChange}
                  placeholder="1-5"
                />
              </div>

              <div className="form-group">
                <label htmlFor="clarity">Clarity</label>
                <input
                  type="number"
                  id="clarity"
                  name="clarity"
                  min="1"
                  max="5"
                  value={reviewForm.clarity}
                  onChange={handleFormChange}
                  placeholder="1-5"
                />
              </div>

              <div className="form-group">
                <label htmlFor="originality">Originality</label>
                <input
                  type="number"
                  id="originality"
                  name="originality"
                  min="1"
                  max="5"
                  value={reviewForm.originality}
                  onChange={handleFormChange}
                  placeholder="1-5"
                />
              </div>

              <div className="form-group">
                <label htmlFor="significance">Significance</label>
                <input
                  type="number"
                  id="significance"
                  name="significance"
                  min="1"
                  max="5"
                  value={reviewForm.significance}
                  onChange={handleFormChange}
                  placeholder="1-5"
                />
              </div>
            </fieldset>

            <div className="form-group">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={submitting || !isCommentValid}
              >
                {submitting ? 'Submitting...' : 'Submit Review'}
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => navigate('/reviews/pending')}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>

        {/* Right Panel: Timeline */}
        <div className="card">
          <h3>Review Timeline</h3>
          <div className="timeline">
            <div className="timeline-item">
              <div className="timeline-marker"></div>
              <div>
                <strong>Submitted</strong>
                <p className="text-muted">
                  {new Date(submission.submissionDate).toLocaleDateString()}
                </p>
              </div>
            </div>

            {submission.reviewDueDate && (
              <div className="timeline-item">
                <div className="timeline-marker"></div>
                <div>
                  <strong>Review Due</strong>
                  <p className="text-muted">
                    {new Date(submission.reviewDueDate).toLocaleDateString()}
                  </p>
                </div>
              </div>
            )}

            <div className="timeline-item">
              <div className="timeline-marker"></div>
              <div>
                <strong>Review Status</strong>
                <p className="text-muted">
                  {submission.completedReviews}/{submission.totalReviewersAssigned} reviewers completed
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReviewDetailPage;
