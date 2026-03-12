import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getPapersByProject, deletePaper } from '../api';

export default function PaperList() {
  const { projectId } = useParams();
  const [papers, setPapers] = useState([]);

  useEffect(() => {
    getPapersByProject(projectId).then((res) => setPapers(res.data));
  }, [projectId]);

  const handleDelete = async (id) => {
    await deletePaper(id);
    setPapers((prev) => prev.filter((p) => p.id !== id));
  };

  return (
    <>
      <h1>📝 Research Papers</h1>
      <Link to={`/projects/${projectId}/papers/new`} className="btn btn-primary" style={{ marginBottom: '1rem' }}>+ New Paper</Link>

      {papers.length === 0 && <p>No papers yet. Create your first research paper!</p>}

      {papers.map((paper) => (
        <div key={paper.id} className="card">
          <h3>{paper.title}</h3>
          <span className={`badge ${paper.status === 'DRAFT' ? 'badge-warning' : paper.status === 'IN_REVIEW' ? 'badge-info' : 'badge-success'}`}>
            {paper.status}
          </span>
          <p style={{ marginTop: '0.5rem' }}>
            <strong>Sections:</strong> {paper.sections?.length || 0} | <strong>Feedback:</strong> {paper.feedbacks?.length || 0}
          </p>
          <div className="actions">
            <Link to={`/papers/${paper.id}`} className="btn btn-primary">View / Edit</Link>
            <Link to={`/papers/${paper.id}/plagiarism`} className="btn btn-primary">🔒 Plagiarism</Link>
            <Link to={`/papers/${paper.id}/export?projectId=${projectId}`} className="btn btn-primary">📤 Export</Link>
            <button className="btn btn-danger" onClick={() => handleDelete(paper.id)}>Delete</button>
          </div>
        </div>
      ))}

      <Link to={`/projects/${projectId}`} className="btn btn-secondary">← Back to Project</Link>
    </>
  );
}
