import axios from 'axios';
import { clearAuthSession, getSessionToken } from './authStorage';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use((config) => {
  // Only set Content-Type: application/json if NOT sending FormData
  // FormData must NOT have a Content-Type header so axios can set the multipart boundary
  if (!(config.data instanceof FormData)) {
    config.headers['Content-Type'] = 'application/json';
  }
  
  const token = getSessionToken();
  if (token) {
    config.headers['X-Session-Token'] = token;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      clearAuthSession();
    }
    return Promise.reject(error);
  }
);

// Authentication (FR1)
export const registerUser = (data) => api.post('/auth/register', data);

export const loginUser = (data) => api.post('/auth/login', data);

export const logoutUser = () => api.post('/auth/logout');

export const getCurrentUser = () => api.get('/auth/me');

// ── Metrics Dashboard ──
export const getMetricsDashboards = () => api.get('/metrics-dashboards');

export const getMetricsDashboardByScope = (scopeType, scopeId) =>
  api.get('/metrics-dashboards/scope', { params: { scopeType, scopeId } });

// ── Analytics ──
export const getAuditLogs = () => api.get('/logs');

// ── Notifications ──
export const getNotifications = () => api.get('/notifications');

export const sendNotification = (data) => api.post('/notifications/send', data);

export const notifyPaperSubmitted = (data) =>
  api.post('/notifications/events/paper-submitted', data);

export const notifyReviewerAssigned = (data) =>
  api.post('/notifications/events/reviewer-assigned', data);

export const notifyFeedbackSubmitted = (data) =>
  api.post('/notifications/events/feedback-submitted', data);

export const notifyReviewCompleted = (data) =>
  api.post('/notifications/events/review-completed', data);

// ── UC1: Projects ──
export const getProjects = (ownerId) =>
  api.get('/projects', { params: ownerId ? { ownerId } : {} });

export const getProject = (id) => api.get(`/projects/${id}`);

export const createProject = (data) => api.post('/projects', data);

export const deleteProject = (id) => api.delete(`/projects/${id}`);

// ── UC2: Search ──
export const searchPapers = (params) => api.get('/search', { params });

export const getAllPapers = () => api.get('/search/all');

// ── UC3: Credibility ──
export const getCredibility = (paperId) =>
  api.get(`/search/credibility/${paperId}`);

// ── UC4: References ──
export const getReferences = (projectId) =>
  api.get(`/references/${projectId}`);

export const addReference = (data) => api.post('/references', data);

export const deleteReference = (id) => api.delete(`/references/${id}`);

export const getSuggestions = (projectId) =>
  api.get(`/references/${projectId}/suggestions`);

// ── UC6: Papers & Feedback ──
export const getPapersByProject = (projectId) =>
  api.get(`/papers/project/${projectId}`);

export const getPaper = (paperId) => api.get(`/papers/${paperId}`);

export const createPaper = (data) => api.post('/papers', data);

export const deletePaper = (paperId) => api.delete(`/papers/${paperId}`);

export const addSection = (paperId, data) =>
  api.post(`/papers/${paperId}/sections`, data);

export const submitFeedback = (paperId, data) =>
  api.post(`/papers/${paperId}/feedback`, data);

export const approvePaper = (paperId) =>
  api.post(`/papers/${paperId}/approve`);

// ── UC7: Plagiarism ──
export const checkPlagiarism = (paperId) =>
  api.get(`/plagiarism/${paperId}`);

// ── UC8: Export ──
export const checkReadiness = (paperId, projectId) =>
  api.get(`/export/readiness/${paperId}`, { params: { projectId } });

export const exportPDF = (paperId) =>
  api.get(`/export/pdf/${paperId}`, { responseType: 'blob' });

export const exportWord = (paperId) =>
  api.get(`/export/word/${paperId}`, { responseType: 'blob' });

export const exportLatex = (paperId) =>
  api.get(`/export/latex/${paperId}`, { responseType: 'blob' });

// ── Submissions ──
export const getSubmissions = ({ authorId, projectId }) =>
  api.get('/submissions', { params: { authorId, projectId } });

export const createSubmission = (data) => api.post('/submissions', data);

export const getSubmission = (submissionId) => api.get(`/submissions/${submissionId}`);

export const getSubmissionVersions = (submissionId) =>
  api.get(`/submissions/${submissionId}/versions`);

export const getSubmissionReviews = (submissionId) =>
  api.get(`/submissions/${submissionId}/reviews`);

export const getSubmissionFeedback = (submissionId) =>
  api.get(`/submissions/${submissionId}/feedback`);

export const submitSubmission = (submissionId) =>
  api.post(`/submissions/${submissionId}/submit`);

export const publishSubmission = (submissionId, data) =>
  api.post(`/submissions/${submissionId}/publish`, data);

// ── Reviews ──
export const getReviews = () => api.get('/reviews');

export const getReviewsByReviewer = (reviewerUserId) =>
  api.get(`/reviews/reviewer/${reviewerUserId}`);

export const getReviewsBySubmission = (submissionId) =>
  api.get(`/reviews/submission/${submissionId}`);

export const createReview = (data) => api.post('/reviews', data);

// ── Review Schedules ──
export const getReviewSchedules = () => api.get('/review-schedules');

export const getReviewSchedulesByReviewer = (reviewerUserId) =>
  api.get(`/review-schedules/reviewer/${reviewerUserId}`);

export const getReviewSchedulesBySubmission = (submissionId) =>
  api.get(`/review-schedules/submission/${submissionId}`);

export const createReviewSchedule = (data) => api.post('/review-schedules', data);

export const completeReviewSchedule = (id) => api.post(`/review-schedules/${id}/complete`);

// ── New Review Submission Functions ──

// Get pending reviews for the current reviewer
export const getMyPendingReviews = () => {
  const auth = JSON.parse(localStorage.getItem('draftly.auth') || '{}');
  return api.get('/reviews/pending/my-reviews', {
    headers: { 'X-User-Id': auth.userId || '' }
  });
};

// Get submission review context (with blind review - no author info)
export const getSubmissionReviewContext = (submissionId) => {
  const auth = JSON.parse(localStorage.getItem('draftly.auth') || '{}');
  return api.get(`/reviews/submission/${submissionId}/context`, {
    headers: { 'X-User-Id': auth.userId || '' }
  });
};

// Submit a review with validation
export const submitReview = (reviewData) => {
  const auth = JSON.parse(localStorage.getItem('draftly.auth') || '{}');
  return api.post('/reviews/submit', reviewData, {
    headers: { 'X-User-Id': auth.userId || '' }
  });
};

export default api;

// ── UC4: References - Export ──
export const exportReferencesBibtex = (projectId) =>
  api.get(`/references/${projectId}/export/bibtex`, { responseType: 'blob' });

export const exportReferencesPlaintext = (projectId, format = 'APA') =>
  api.get(`/references/${projectId}/export/plaintext`, { params: { format }, responseType: 'blob' });

export const exportReferencesHtml = (projectId, format = 'APA') =>
  api.get(`/references/${projectId}/export/html`, { params: { format }, responseType: 'blob' });
