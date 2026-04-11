import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <>
      <div className="hero">
        <h1>Welcome to Draftly</h1>
        <p>Your Academic Research Assistance Platform</p>
        <Link to="/projects" className="btn btn-primary hero-cta">Get Started →</Link>
      </div>
      <div className="features">
        <div className="card feature-card">
          <div className="feature-icon">📁</div>
          <h3>Create Projects</h3>
          <p>Create research projects with auto-suggested keywords using NLP.</p>
        </div>
        <div className="card feature-card">
          <div className="feature-icon">🔍</div>
          <h3>Discover Literature</h3>
          <p>Search and rank papers by relevance using TF-IDF similarity.</p>
        </div>
        <div className="card feature-card">
          <div className="feature-icon">⭐</div>
          <h3>Evaluate Credibility</h3>
          <p>Score papers based on citations, journal impact, and recency.</p>
        </div>
        <div className="card feature-card">
          <div className="feature-icon">📚</div>
          <h3>Manage References</h3>
          <p>Save references and generate APA/MLA/IEEE citations.</p>
        </div>
        <div className="card feature-card">
          <div className="feature-icon">💬</div>
          <h3>Review & Feedback</h3>
          <p>Faculty can review sections and approve or request revisions.</p>
        </div>
        <div className="card feature-card">
          <div className="feature-icon">📤</div>
          <h3>Export Paper</h3>
          <p>Validate readiness and export as PDF, Word, or LaTeX.</p>
        </div>
      </div>
    </>
  );
}
