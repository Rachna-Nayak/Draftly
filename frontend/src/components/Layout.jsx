import { NavLink, Outlet } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { logoutUser } from '../api';
import { clearAuthSession, getCurrentUser } from '../authStorage';
import { getNavItemsForRole, getRoleDisplayName } from '../roleAccess';
import './Layout.css';

export default function Layout() {
  const navigate = useNavigate();
  const currentUser = getCurrentUser();
  const navItems = getNavItemsForRole(currentUser?.role);

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
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to}>{item.label}</NavLink>
        ))}
        <div className="nav-user">
          <span>{currentUser?.name || currentUser?.email}</span>
          <span className="role-badge">{getRoleDisplayName(currentUser?.role)}</span>
          <button className="btn btn-secondary" type="button" onClick={handleLogout}>Logout</button>
        </div>
      </nav>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}
