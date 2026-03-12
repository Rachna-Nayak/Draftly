import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { checkPlagiarism } from '../api';

export default function PlagiarismReport() {
  const { paperId } = useParams();
  const [report, setReport] = useState(null);

  useEffect(() => {
    checkPlagiarism(paperId).then((res) => setReport(res.data));
  }, [paperId]);

  if (!report) return <p>Analyzing...</p>;

  const pct = ((report.overallSimilarityScore || 0) * 100).toFixed(1);
  const isHigh = report.overallSimilarityScore > 0.5;

  return (
    <>
      <h1>🔒 Plagiarism Report</h1>

      <div className="card">
        <h2>Overall Similarity</h2>
        <p>Score: <strong style={{ color: isHigh ? '#e74c3c' : '#27ae60', fontSize: '1.3rem' }}>{pct}%</strong></p>
        <div className="score-bar">
          <div style={{ height: '100%', borderRadius: '10px', width: `${pct}%`, background: isHigh ? '#e74c3c' : '#27ae60' }} />
        </div>
      </div>

      {report.flaggedSections?.length > 0 ? (
        <>
          <h2>⚠️ Flagged Sections</h2>
          {report.flaggedSections.map((flag, i) => (
            <div key={i} className="flagged">
              <p><strong>Section:</strong> {flag.sectionName}</p>
              <p><strong>Matched Paper:</strong> {flag.matchedPaperTitle}</p>
              <p><strong>Similarity:</strong> {(flag.similarityScore * 100).toFixed(1)}%</p>
            </div>
          ))}
        </>
      ) : (
        <div className="card">
          <p style={{ color: '#27ae60', fontWeight: 'bold' }}>✅ No plagiarism flags detected. Your paper looks original!</p>
        </div>
      )}

      <Link to={`/papers/${paperId}`} className="btn btn-secondary" style={{ marginTop: '1rem' }}>← Back to Paper</Link>
    </>
  );
}
