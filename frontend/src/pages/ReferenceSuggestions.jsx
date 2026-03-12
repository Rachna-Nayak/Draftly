import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getSuggestions, addReference } from '../api';

export default function ReferenceSuggestions() {
  const { projectId } = useParams();
  const [suggestions, setSuggestions] = useState([]);

  useEffect(() => {
    getSuggestions(projectId).then((res) => setSuggestions(res.data));
  }, [projectId]);

  const handleAdd = async (paperId, format) => {
    await addReference({ projectId, paperId, format });
    setSuggestions((prev) => prev.filter((p) => p.id !== paperId));
  };

  return (
    <>
      <h1>💡 Suggested References</h1>

      {suggestions.length === 0 && <p>No suggestions found. Try adding more keywords to your project.</p>}

      {suggestions.map((paper) => (
        <div key={paper.id} className="card">
          <h3>{paper.title}</h3>
          <p><strong>Authors:</strong> {paper.authors?.join(', ')}</p>
          <p><strong>Year:</strong> {paper.publicationYear} | <strong>Citations:</strong> {paper.citationCount}</p>
          <div className="actions" style={{ marginTop: '0.5rem' }}>
            <button className="btn btn-success" onClick={() => handleAdd(paper.id, 'APA')}>+ APA</button>
            <button className="btn btn-success" onClick={() => handleAdd(paper.id, 'MLA')}>+ MLA</button>
            <button className="btn btn-success" onClick={() => handleAdd(paper.id, 'IEEE')}>+ IEEE</button>
          </div>
        </div>
      ))}

      <Link to={`/projects/${projectId}/references`} className="btn btn-secondary">← Back to References</Link>
    </>
  );
}
