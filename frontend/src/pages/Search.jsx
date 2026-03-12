import { useState } from 'react';
import { Link } from 'react-router-dom';
import { searchPapers } from '../api';

export default function Search() {
  const [keywords, setKeywords] = useState('');
  const [domain, setDomain] = useState('');
  const [startYear, setStartYear] = useState('');
  const [endYear, setEndYear] = useState('');
  const [minCitations, setMinCitations] = useState('');
  const [papers, setPapers] = useState([]);

  const handleSearch = async (e) => {
    e.preventDefault();
    const params = { keywords };
    if (domain) params.domain = domain;
    if (startYear) params.startYear = startYear;
    if (endYear) params.endYear = endYear;
    if (minCitations) params.minCitations = minCitations;
    const res = await searchPapers(params);
    setPapers(res.data);
  };

  return (
    <>
      <h1>🔍 Discover Relevant Literature</h1>
      <div className="card">
        <form onSubmit={handleSearch}>
          <div className="form-group">
            <label>Keywords (comma-separated)</label>
            <input value={keywords} onChange={(e) => setKeywords(e.target.value)} required placeholder="e.g., machine learning, neural networks, healthcare" />
          </div>
          <div className="filters">
            <div className="form-group">
              <label>Domain</label>
              <input value={domain} onChange={(e) => setDomain(e.target.value)} placeholder="e.g., Computer Science" />
            </div>
            <div className="form-group">
              <label>From Year</label>
              <input type="number" value={startYear} onChange={(e) => setStartYear(e.target.value)} placeholder="2015" />
            </div>
            <div className="form-group">
              <label>To Year</label>
              <input type="number" value={endYear} onChange={(e) => setEndYear(e.target.value)} placeholder="2026" />
            </div>
            <div className="form-group">
              <label>Min Citations</label>
              <input type="number" value={minCitations} onChange={(e) => setMinCitations(e.target.value)} placeholder="0" />
            </div>
          </div>
          <button type="submit" className="btn btn-primary">Search</button>
        </form>
      </div>

      {papers.length > 0 && (
        <>
          <h2>Search Results</h2>
          <table>
            <thead>
              <tr><th>Title</th><th>Authors</th><th>Year</th><th>Citations</th><th>Journal</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {papers.map((paper) => (
                <tr key={paper.id}>
                  <td>{paper.title}</td>
                  <td>{paper.authors?.join(', ')}</td>
                  <td>{paper.publicationYear}</td>
                  <td>{paper.citationCount}</td>
                  <td>{paper.journal}</td>
                  <td><Link to={`/search/credibility/${paper.id}`} className="btn btn-success">⭐ Credibility</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </>
  );
}
