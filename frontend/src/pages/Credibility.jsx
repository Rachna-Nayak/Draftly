import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCredibility } from '../api';

export default function Credibility() {
  const { paperId } = useParams();
  const [data, setData] = useState(null);

  useEffect(() => {
    getCredibility(paperId).then((res) => setData(res.data));
  }, [paperId]);

  if (!data) return <p>Loading...</p>;

  const { paper, report } = data;

  return (
    <>
      <h1>⭐ Credibility Report</h1>
      <div className="card">
        <h2>{paper.title}</h2>
        <p><strong>Journal:</strong> {paper.journal}</p>
        <p><strong>Year:</strong> {paper.publicationYear}</p>
        <p><strong>Citations:</strong> {paper.citationCount}</p>
      </div>

      <div className="card">
        <h2>Scores</h2>
        <p><strong>Citation Score:</strong> {report.citationScore?.toFixed(2)}</p>
        <div className="score-bar"><div className="score-fill" style={{ width: `${(report.citationScore || 0) * 100}%` }} /></div>

        <p><strong>Journal Impact Score:</strong> {report.journalScore?.toFixed(2)}</p>
        <div className="score-bar"><div className="score-fill" style={{ width: `${(report.journalScore || 0) * 100}%` }} /></div>

        <p><strong>Recency Score:</strong> {report.recencyScore?.toFixed(2)}</p>
        <div className="score-bar"><div className="score-fill" style={{ width: `${(report.recencyScore || 0) * 100}%` }} /></div>

        <hr style={{ margin: '1rem 0' }} />
        <p>Overall Credibility: <strong style={{ color: '#27ae60', fontSize: '1.4rem' }}>{report.overallScore?.toFixed(2)}</strong></p>
      </div>

      {report.suggestedCitation && (
        <div className="card">
          <h2>Suggested Citation (APA)</h2>
          <div className="citation-box">{report.suggestedCitation}</div>
        </div>
      )}

      <Link to="/search" className="btn btn-secondary">← Back to Search</Link>
    </>
  );
}
