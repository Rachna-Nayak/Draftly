import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createProject } from '../api';

export default function ProjectCreate() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ title: '', domain: '', objectives: '' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    await createProject({ ...form, ownerId: 'default' });
    navigate('/projects');
  };

  return (
    <>
      <h1>Create New Research Project</h1>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Project Title</label>
            <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required placeholder="e.g., Machine Learning in Healthcare" />
          </div>
          <div className="form-group">
            <label>Domain</label>
            <input value={form.domain} onChange={(e) => setForm({ ...form, domain: e.target.value })} required placeholder="e.g., Computer Science" />
          </div>
          <div className="form-group">
            <label>Objectives</label>
            <textarea value={form.objectives} onChange={(e) => setForm({ ...form, objectives: e.target.value })} required placeholder="Describe your research objectives..." />
          </div>
          <button type="submit" className="btn btn-primary">Create Project</button>{' '}
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/projects')}>Cancel</button>
        </form>
      </div>
    </>
  );
}
