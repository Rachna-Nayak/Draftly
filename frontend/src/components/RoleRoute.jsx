import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { getCurrentUserRole, getSessionToken } from '../authStorage';
import { normalizeRole } from '../roleAccess';

export default function RoleRoute({ allowedRoles = [], children }) {
  const location = useLocation();
  const token = getSessionToken();
  const role = normalizeRole(getCurrentUserRole());
  const isDevAuthBypassEnabled = import.meta.env.VITE_BYPASS_AUTH === 'true';

  if (isDevAuthBypassEnabled) {
    return children || <Outlet />;
  }

  if (!token) {
    return <Navigate to="/login" replace state={{ from: `${location.pathname}${location.search}` }} />;
  }

  const normalizedAllowedRoles = allowedRoles.map((allowedRole) => normalizeRole(allowedRole));
  if (normalizedAllowedRoles.length > 0 && !normalizedAllowedRoles.includes(role)) {
    return <Navigate to="/unauthorized" replace state={{ from: `${location.pathname}${location.search}` }} />;
  }

  return children || <Outlet />;
}