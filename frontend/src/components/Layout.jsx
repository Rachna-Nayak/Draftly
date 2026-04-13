import { NavLink, Outlet } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { logoutUser } from '../api';
import { clearAuthSession, getCurrentUser, getCurrentUserRole, hasAnyRole } from '../authStorage';
import { getRoleDisplayName } from '../roleAccess';
import './Layout.css';

export default function Layout() {
  const navigate = useNavigate();
  const currentUser = getCurrentUser();
  const currentRole = getCurrentUserRole();
  const isDevAuthBypassEnabled = import.meta.env.VITE_BYPASS_AUTH === 'true';
  const canAccess = (roles) => hasAnyRole(roles);

  const handleLogout = async () => {
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

        {canAccess(['AUTHOR', 'REVIEWER', 'ADMIN']) && (
          <>
            <NavLink to="/projects">Projects</NavLink>
            <NavLink to="/search">Search Literature</NavLink>
            <NavLink to="/converter">📄 DOCX → LaTeX</NavLink>
            <NavLink to="/submissions">Submissions</NavLink>
            <NavLink to="/metrics">Metrics</NavLink>
          </>
        )}

        {canAccess(['AUTHOR', 'ADMIN']) && (
          <NavLink to="/submissions/new">Create Submission</NavLink>
        )}

        {canAccess(['REVIEWER', 'ADMIN']) && (
          <>
            <NavLink to="/notifications">Notifications</NavLink>
            <NavLink to="/review-queue">Review Queue</NavLink>
          </>
        )}

        {canAccess(['ADMIN']) && (
          <>
            <NavLink to="/reviewer-assignment">Reviewer Assignment</NavLink>
            <NavLink to="/analytics">Analytics</NavLink>
          </>
        )}

        <div className="nav-user">
          <span>
            {currentUser
              ? `${currentUser?.name || currentUser?.email}${currentRole ? ` (${getRoleDisplayName(currentRole)})` : ''}`
              : (isDevAuthBypassEnabled ? 'Demo mode' : 'Signed in')}
          </span>
          <button className="btn btn-secondary" type="button" onClick={handleLogout}>Logout</button>
        </div>
      </nav>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}
