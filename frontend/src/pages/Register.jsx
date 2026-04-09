import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../api';
import { setAuthSession } from '../authStorage';

const roleOptions = [
  { label: 'Author', value: 'STUDENT_RESEARCHER' },
  { label: 'Reviewer', value: 'FACULTY_SUPERVISOR' },
  { label: 'Administrator', value: 'SYSTEM_ADMINISTRATOR' },
];

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'STUDENT_RESEARCHER',
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
    <>
      <h1>Create your Draftly account</h1>
      <div className="card auth-card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full name</label>
            <input
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
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

          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p className="auth-hint">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </>
  );
}
