import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getReferences, deleteReference } from '../api';

export default function ReferenceList() {
  const { projectId } = useParams();
  const [references, setReferences] = useState([]);

  useEffect(() => {
    getReferences(projectId).then((res) => setReferences(res.data));
  }, [projectId]);

  const handleDelete = async (id) => {
    await deleteReference(id);
    setReferences((prev) => prev.filter((r) => r.id !== id));
  };

  return (
    <>
      <h1>📚 Project References</h1>
      <Link to={`/projects/${projectId}/references/suggestions`} className="btn btn-primary" style={{ marginBottom: '1rem' }}>💡 Get Suggestions</Link>

      {references.length === 0 && <p>No references added yet. Search for papers and add them as references.</p>}

      {references.map((ref) => (
        <div key={ref.id} className="card">
          <span className="badge badge-success">{ref.citationFormat}</span>
          <div className="citation-box" style={{ marginTop: '0.5rem' }}>{ref.formattedCitation}</div>
          <button className="btn btn-danger" style={{ marginTop: '0.5rem' }} onClick={() => handleDelete(ref.id)}>Remove</button>
        </div>
      ))}

      <Link to={`/projects/${projectId}`} className="btn btn-secondary">← Back to Project</Link>
    </>
  );
}
