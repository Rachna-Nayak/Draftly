import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// ── UC1: Projects ──
export const getProjects = (ownerId = 'default') =>
  api.get('/projects', { params: { ownerId } });

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

export default api;
