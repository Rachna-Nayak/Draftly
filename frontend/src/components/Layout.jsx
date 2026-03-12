import { NavLink, Outlet } from 'react-router-dom';
import './Layout.css';

export default function Layout() {
  return (
    <>
      <nav className="navbar">
        <NavLink to="/" className="brand">📝 Draftly</NavLink>
        <NavLink to="/projects">Projects</NavLink>
        <NavLink to="/search">Search Literature</NavLink>
      </nav>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}
