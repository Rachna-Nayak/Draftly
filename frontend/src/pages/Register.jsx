import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../api';
import { setAuthSession } from '../authStorage';

const roleOptions = [
  { label: 'Author', value: 'AUTHOR' },
  { label: 'Reviewer', value: 'REVIEWER' },
  { label: 'Administrator', value: 'ADMIN' },
];

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'AUTHOR',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await registerUser(form);
      setAuthSession(response.data);
      navigate('/projects', { replace: true });
    } catch (err) {
      const message = err?.response?.data?.message || 'Registration failed. Please try again with a different email.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-shell">
      <div className="auth-hero-panel">
        <p className="auth-kicker">Research Onboarding</p>
        <h1>Create your Draftly account</h1>
        <p className="auth-subtitle">
          Join as an author, reviewer, or administrator and start collaborating on academic writing workflows.
        </p>
        <ul className="auth-feature-list">
          <li>Organize projects, papers, and references</li>
          <li>Share section-level feedback and decisions</li>
          <li>Prepare submissions with export validation</li>
        </ul>
      </div>

      <div className="card auth-card">
        <div className="auth-card-head">
          <h2>Register</h2>
          <p>Create your profile to get started.</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full name</label>
            <input
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
              placeholder="Your name"
            />
          </div>

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
              placeholder="At least 8 characters"
            />
          </div>

          <div className="form-group">
            <label>Role</label>
            <select
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}
            >
              {roleOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          {error && <p className="error-text">{error}</p>}

          <button className="btn btn-primary auth-submit" type="submit" disabled={loading}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p className="auth-hint">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </section>
  );
}
