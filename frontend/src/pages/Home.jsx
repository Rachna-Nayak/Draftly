import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <>
      <div className="hero">
        <h1>Welcome to Draftly</h1>
        <p>Your Academic Research Assistance Platform</p>
        <Link to="/projects" className="btn btn-primary">Get Started →</Link>
      </div>
      <div className="features">
        <div className="card"><h2>📁 UC1: Create Projects</h2><p>Create research projects with auto-suggested keywords using NLP.</p></div>
        <div className="card"><h2>🔍 UC2: Discover Literature</h2><p>Search and rank papers by relevance using TF-IDF similarity.</p></div>
        <div className="card"><h2>⭐ UC3: Evaluate Credibility</h2><p>Score papers based on citations, journal impact, and recency.</p></div>
        <div className="card"><h2>📚 UC4: Manage References</h2><p>Save references and generate APA/MLA/IEEE citations.</p></div>
        <div className="card"><h2>💬 UC6: Review &amp; Feedback</h2><p>Faculty can review sections and approve or request revisions.</p></div>
        <div className="card"><h2>🔒 UC7: Plagiarism Check</h2><p>Detect similarity against existing papers in the database.</p></div>
        <div className="card"><h2>📤 UC8: Export Paper</h2><p>Validate readiness and export as PDF, Word, or LaTeX.</p></div>
      </div>
    </>
  );
}
