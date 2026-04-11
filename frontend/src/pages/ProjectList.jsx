import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProjects, deleteProject } from '../api';
import { getCurrentUserId } from '../authStorage';

export default function ProjectList() {
  const [projects, setProjects] = useState([]);

  useEffect(() => {
    const ownerId = getCurrentUserId();
    getProjects(ownerId || 'default').then((res) => setProjects(res.data));
  }, []);

  const handleDelete = async (id) => {
    await deleteProject(id);
    setProjects((prev) => prev.filter((p) => p.id !== id));
  };

  return (
    <>
      <h1>My Research Projects</h1>
      <Link to="/projects/new" className="btn btn-primary" style={{ marginBottom: '1.5rem' }}>+ New Project</Link>

      {projects.length === 0 && <p>No projects yet. Create your first research project!</p>}

      {projects.map((project) => (
        <div key={project.id} className="card">
          <h3>{project.title}</h3>
          <p><strong>Domain:</strong> {project.domain}</p>
          <p>
            <strong>Keywords:</strong>{' '}
            {project.keywords?.map((kw) => (
              <span key={kw} className="badge badge-info">{kw}</span>
            ))}
          </p>
          <div className="actions">
            <Link to={`/projects/${project.id}`} className="btn btn-primary">View</Link>
            <Link to={`/projects/${project.id}/papers`} className="btn btn-primary">Papers</Link>
            <Link to={`/projects/${project.id}/references`} className="btn btn-primary">References</Link>
            <button className="btn btn-danger" onClick={() => handleDelete(project.id)}>Delete</button>
          </div>
        </div>
      ))}
    </>
  );
}
