import { useEffect, useState } from 'react';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import { checkReadiness, exportPDF, exportWord, exportLatex } from '../api';

export default function ExportReadiness() {
  const { paperId } = useParams();
  const [searchParams] = useSearchParams();
  const projectId = searchParams.get('projectId');
  const [report, setReport] = useState(null);

  useEffect(() => {
    if (projectId) {
      checkReadiness(paperId, projectId).then((res) => setReport(res.data));
    }
  }, [paperId, projectId]);

  const download = async (fetcher, filename) => {
    const res = await fetcher(paperId);
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  if (!report) return <p>Checking readiness...</p>;

  return (
    <>
      <h1>📤 Export &amp; Readiness Check</h1>

      <div className="card">
        <h2>Readiness Status</h2>
        {report.ready ? (
          <p style={{ color: '#27ae60', fontSize: '1.2rem', fontWeight: 'bold' }}>✅ Paper is ready for submission!</p>
        ) : (
          <p style={{ color: '#e74c3c', fontSize: '1.2rem', fontWeight: 'bold' }}>❌ Paper is not ready. Fix the issues below:</p>
        )}
        {report.issues?.length > 0 && (
          <div style={{ marginTop: '1rem' }}>
            {report.issues.map((issue, i) => (
              <div key={i} className="issue"><p>{issue}</p></div>
            ))}
          </div>
        )}
      </div>

      {report.ready && (
        <div className="card">
          <h2>Export Options</h2>
          <div className="actions">
            <button className="btn btn-primary" onClick={() => download(exportPDF, 'paper.pdf')}>📄 Export as PDF</button>
            <button className="btn btn-primary" onClick={() => download(exportWord, 'paper.docx')}>📝 Export as Word</button>
            <button className="btn btn-primary" onClick={() => download(exportLatex, 'paper.tex')}>📐 Export as LaTeX</button>
          </div>
        </div>
      )}

      <Link to={`/papers/${paperId}`} className="btn btn-secondary" style={{ marginTop: '1rem' }}>← Back to Paper</Link>
    </>
  );
}
