import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { getSessionToken } from '../authStorage';

export default function ProtectedRoute() {
  const location = useLocation();
  const token = getSessionToken();
  const isDevAuthBypassEnabled = import.meta.env.VITE_BYPASS_AUTH === 'true';

  if (isDevAuthBypassEnabled) {
    return <Outlet />;
  }

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
