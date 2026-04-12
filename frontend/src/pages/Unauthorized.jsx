import { Link, useLocation } from 'react-router-dom';

export default function Unauthorized() {
  const location = useLocation();
  const attemptedPath = location.state?.from || '/';

  return (
    <section className="auth-shell">
      <div className="auth-hero-panel">
        <p className="auth-kicker">Access Restricted</p>
        <h1>Role-based access is enabled</h1>
        <p className="auth-subtitle">
          The page you tried to open is limited to a different role.
          Use an account with the correct role or return to your dashboard.
        </p>
      </div>

      <div className="card auth-card">
        <div className="auth-card-head">
          <h2>Unauthorized</h2>
          <p>Attempted path: {attemptedPath}</p>
        </div>

        <div className="actions">
          <Link className="btn btn-primary" to="/">
            Go to dashboard
          </Link>
          <Link className="btn btn-secondary" to="/login">
            Sign in again
          </Link>
        </div>
      </div>
    </section>
  );
}