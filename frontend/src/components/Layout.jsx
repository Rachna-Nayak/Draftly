import { NavLink, Outlet } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { logoutUser } from '../api';
import { clearAuthSession, getCurrentUser } from '../authStorage';
import './Layout.css';

export default function Layout() {
  const navigate = useNavigate();
  const currentUser = getCurrentUser();

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
        <NavLink to="/projects">Projects</NavLink>
        <NavLink to="/search">Search Literature</NavLink>
        <div className="nav-user">
          <span>{currentUser?.name || currentUser?.email}</span>
          <button className="btn btn-secondary" type="button" onClick={handleLogout}>Logout</button>
        </div>
      </nav>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}
