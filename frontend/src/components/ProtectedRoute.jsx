import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { getSessionToken } from '../authStorage';

export default function ProtectedRoute() {
  const location = useLocation();
  const token = getSessionToken();

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
