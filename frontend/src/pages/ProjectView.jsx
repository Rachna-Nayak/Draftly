import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getProject } from '../api';

export default function ProjectView() {
  const { id } = useParams();
  const [project, setProject] = useState(null);

  useEffect(() => {
    getProject(id).then((res) => setProject(res.data));
  }, [id]);

  if (!project) return <p>Loading...</p>;

  return (
    <>
      <h1>{project.title}</h1>
      <div className="card">
        <p><strong>Domain:</strong> {project.domain}</p>
        <p><strong>Objectives:</strong> {project.objectives}</p>
        <p>
          <strong>Keywords:</strong>{' '}
          {project.keywords?.map((kw) => (
            <span key={kw} className="badge badge-info">{kw}</span>
          ))}
        </p>
        <p><strong>Created:</strong> {project.createdAt}</p>
      </div>
      <h2>Quick Actions</h2>
      <div className="actions">
        <Link to={`/projects/${id}/papers`} className="btn btn-primary">📝 Papers</Link>
        <Link to={`/projects/${id}/references`} className="btn btn-primary">📚 References</Link>
        <Link to="/search" className="btn btn-primary">🔍 Search Literature</Link>
      </div>
    </>
  );
}
