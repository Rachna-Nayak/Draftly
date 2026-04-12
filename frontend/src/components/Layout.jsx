import { NavLink, Outlet } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { logoutUser } from '../api';
import { clearAuthSession, getCurrentUser } from '../authStorage';
import './Layout.css';

export default function Layout() {
  const navigate = useNavigate();
  const currentUser = getCurrentUser();
  const isDevAuthBypassEnabled = import.meta.env.VITE_BYPASS_AUTH === 'true';

  const handleLogout = async () => {
    if (isDevAuthBypassEnabled) {
      navigate('/');
      return;
    }

    try {
      await logoutUser();
    } catch {
      // Session may already be invalid; continue local cleanup.
    }
    clearAuthSession();
    navigate('/login');
  };

  return (
    <>
      <nav className="navbar">
        <NavLink to="/" className="brand">Draftly</NavLink>
        <NavLink to="/projects">Projects</NavLink>
        <NavLink to="/search">Search Literature</NavLink>
        <NavLink to="/submissions">Submissions</NavLink>
        <NavLink to="/submissions/new">Create Submission</NavLink>
        <NavLink to="/reviewer-assignment">Reviewer Assignment</NavLink>
        <NavLink to="/metrics">Metrics</NavLink>
        <NavLink to="/analytics">Analytics</NavLink>
        <NavLink to="/notifications">Notifications</NavLink>
        <NavLink to="/review-queue">Review Queue</NavLink>
        <div className="nav-user">
          <span>{isDevAuthBypassEnabled ? 'Demo mode' : (currentUser?.name || currentUser?.email)}</span>
          {!isDevAuthBypassEnabled && (
            <button className="btn btn-secondary" type="button" onClick={handleLogout}>Logout</button>
          )}
        </div>
      </nav>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}
