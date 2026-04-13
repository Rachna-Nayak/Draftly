import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getReferences, deleteReference, exportReferencesBibtex, exportReferencesPlaintext, exportReferencesHtml } from '../api';

export default function ReferenceList() {
  const { projectId } = useParams();
  const [references, setReferences] = useState([]);
  const [loading, setLoading] = useState(false);
  const [format, setFormat] = useState('APA');

  useEffect(() => {
    getReferences(projectId).then((res) => setReferences(res.data));
  }, [projectId]);

  const handleDelete = async (id) => {
    await deleteReference(id);
    setReferences((prev) => prev.filter((r) => r.id !== id));
  };

  const handleExportBibtex = async () => {
    try {
      setLoading(true);
      const response = await exportReferencesBibtex(projectId);
      downloadFile(response.data, 'references.bib', 'text/plain');
    } catch (err) {
      console.error('Export failed:', err);
      alert('Failed to export references as BibTeX');
    } finally {
      setLoading(false);
    }
  };

  const handleExportPlaintext = async () => {
    try {
      setLoading(true);
      const response = await exportReferencesPlaintext(projectId, format);
      downloadFile(response.data, `references_${format.toLowerCase()}.txt`, 'text/plain');
    } catch (err) {
      console.error('Export failed:', err);
      alert('Failed to export references as text');
    } finally {
      setLoading(false);
    }
  };

  const handleExportHtml = async () => {
    try {
      setLoading(true);
      const response = await exportReferencesHtml(projectId, format);
      downloadFile(response.data, `references_${format.toLowerCase()}.html`, 'text/html');
    } catch (err) {
      console.error('Export failed:', err);
      alert('Failed to export references as HTML');
    } finally {
      setLoading(false);
    }
  };

  const downloadFile = (data, filename, type) => {
    const blob = new Blob([data], { type });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  };

  return (
    <>
      <h1>📚 Project References</h1>
      <Link to={`/projects/${projectId}/references/suggestions`} className="btn btn-primary" style={{ marginBottom: '1rem' }}>💡 Get Suggestions</Link>

      <div style={{ marginBottom: '2rem', padding: '1rem', backgroundColor: '#f0f0f0', borderRadius: '4px' }}>
        <h3>📥 Export References</h3>
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
          <button className="btn btn-info" onClick={handleExportBibtex} disabled={loading || references.length === 0}>
            {loading ? '⏳ Exporting...' : '📄 BibTeX (.bib)'}
          </button>
          
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <select value={format} onChange={(e) => setFormat(e.target.value)} disabled={loading} style={{ padding: '0.5rem' }}>
              <option>APA</option>
              <option>MLA</option>
              <option>IEEE</option>
            </select>
            <button className="btn btn-info" onClick={handleExportPlaintext} disabled={loading || references.length === 0}>
              {loading ? '⏳ Exporting...' : '📝 Text (.txt)'}
            </button>
            <button className="btn btn-info" onClick={handleExportHtml} disabled={loading || references.length === 0}>
              {loading ? '⏳ Exporting...' : '🌐 HTML (.html)'}
            </button>
          </div>
        </div>
      </div>

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
