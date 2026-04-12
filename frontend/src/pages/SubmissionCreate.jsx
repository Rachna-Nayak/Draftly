import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { createSubmission, getProjects } from '../api';
import { getCurrentUserId } from '../authStorage';

export default function SubmissionCreate() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const currentUserId = getCurrentUserId();

  const [projects, setProjects] = useState([]);
  const [loadingProjects, setLoadingProjects] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [form, setForm] = useState({
    projectId: searchParams.get('projectId') || '',
    authorId: currentUserId || '',
    title: '',
    conferenceName: '',
    track: '',
    docxPath: '',
  });

  useEffect(() => {
    const ownerId = currentUserId || 'default';
    setLoadingProjects(true);
    getProjects(ownerId)
      .then((response) => {
        const list = Array.isArray(response?.data) ? response.data : [];
        setProjects(list);
        if (!form.projectId && list.length > 0) {
          setForm((prev) => ({ ...prev, projectId: list[0].id }));
        }
      })
      .catch((err) => {
        setError(err?.response?.data?.message || err?.message || 'Unable to load projects.');
      })
      .finally(() => setLoadingProjects(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const projectOptions = useMemo(() => projects.map((project) => ({ id: project.id, title: project.title })), [projects]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    try {
      await createSubmission({
        projectId: form.projectId,
        authorId: form.authorId || currentUserId || 'default',
        title: form.title,
        conferenceName: form.conferenceName,
        track: form.track,
        docxPath: form.docxPath,
      });
      setMessage('Submission created successfully.');
      navigate('/submissions');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Unable to create submission.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="card metrics-header">
        <div>
          <h2>Create Submission</h2>
          <p className="metrics-subtitle">Start a research submission under one of your projects.</p>
        </div>
      </div>

      {loadingProjects && <div className="card">Loading projects...</div>}
      {!loadingProjects && error && <div className="card flagged"><strong>Could not load submission form.</strong><div>{error}</div></div>}
      {!loadingProjects && message && <div className="card issue">{message}</div>}

      <div className="card">
        <form onSubmit={handleSubmit} className="page-stack">
          <div className="form-group">
            <label htmlFor="projectId">Project</label>
            <select
              id="projectId"
              value={form.projectId}
              onChange={(event) => setForm({ ...form, projectId: event.target.value })}
              required
            >
              <option value="">Select a project</option>
              {projectOptions.map((project) => (
                <option key={project.id} value={project.id}>{project.title} ({project.id})</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="authorId">Author ID</label>
            <input
              id="authorId"
              value={form.authorId}
              onChange={(event) => setForm({ ...form, authorId: event.target.value })}
              placeholder={currentUserId || 'author-1'}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="title">Submission Title</label>
            <input
              id="title"
              value={form.title}
              onChange={(event) => setForm({ ...form, title: event.target.value })}
              placeholder="e.g., Deep Learning for Clinical Decision Support"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="conferenceName">Conference Name</label>
            <input
              id="conferenceName"
              value={form.conferenceName}
              onChange={(event) => setForm({ ...form, conferenceName: event.target.value })}
              placeholder="e.g., ICML 2026"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="track">Track</label>
            <input
              id="track"
              value={form.track}
              onChange={(event) => setForm({ ...form, track: event.target.value })}
              placeholder="e.g., AI in Healthcare"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="docxPath">DOCX File Path</label>
            <input
              id="docxPath"
              value={form.docxPath}
              onChange={(event) => setForm({ ...form, docxPath: event.target.value })}
              placeholder="/uploads/my-paper.docx"
              required
            />
          </div>

          <div className="actions">
            <button className="btn btn-primary" type="submit" disabled={saving || loadingProjects}>
              {saving ? 'Creating...' : 'Create Submission'}
            </button>
            <button className="btn btn-secondary" type="button" onClick={() => navigate('/submissions')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
