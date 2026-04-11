import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { loginUser } from '../api';
import { setAuthSession } from '../authStorage';

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const redirectTo = location.state?.from || '/projects';

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await loginUser(form);
      setAuthSession(response.data);
      navigate(redirectTo, { replace: true });
    } catch {
      setError('Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-shell">
      <div className="auth-hero-panel">
        <p className="auth-kicker">Academic Workspace</p>
        <h1>Welcome back to Draftly</h1>
        <p className="auth-subtitle">
          Continue your research projects, track paper progress, and prepare export-ready submissions.
        </p>
        <ul className="auth-feature-list">
          <li>Project and paper workflow in one place</li>
          <li>Credibility, references, and plagiarism checkpoints</li>
          <li>Readiness validation before final export</li>
        </ul>
      </div>

      <div className="card auth-card">
        <div className="auth-card-head">
          <h2>Sign in</h2>
          <p>Use your account details to continue.</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
              placeholder="you@university.edu"
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
              minLength={8}
              placeholder="Enter your password"
            />
          </div>

          {error && <p className="error-text">{error}</p>}

          <button className="btn btn-primary auth-submit" type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="auth-hint">
          Need an account? <Link to="/register">Create one</Link>
        </p>
      </div>
    </section>
  );
}
