import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { getSessionToken, hasAnyRole } from '../authStorage';

export default function ProtectedRoute({ roles = [] }) {
  const location = useLocation();
  const token = getSessionToken();
  const isDevAuthBypassEnabled = import.meta.env.VITE_BYPASS_AUTH === 'true';

  if (!token) {
    if (isDevAuthBypassEnabled && roles.length === 0) {
      return <Outlet />;
    }
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (roles.length && !hasAnyRole(roles)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
