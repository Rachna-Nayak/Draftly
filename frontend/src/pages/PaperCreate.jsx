import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { createPaper } from '../api';

export default function PaperCreate() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const [title, setTitle] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    await createPaper({ projectId, title });
    navigate(`/projects/${projectId}/papers`);
  };

  return (
    <>
      <h1>Create New Paper</h1>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Paper Title</label>
            <input value={title} onChange={(e) => setTitle(e.target.value)} required placeholder="e.g., A Survey of Deep Learning Techniques..." />
          </div>
          <button type="submit" className="btn btn-primary">Create Paper</button>{' '}
          <button type="button" className="btn btn-secondary" onClick={() => navigate(`/projects/${projectId}/papers`)}>Cancel</button>
        </form>
      </div>
    </>
  );
}
