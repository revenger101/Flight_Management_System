import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function RoleProtectedRoute({ allowedRoles, children }) {
  const { isReady, isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isReady) {
    return <div className="auth-loading-screen">Authorizing access...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (!allowedRoles?.includes(user?.role)) {
    return <Navigate to="/forbidden" replace />;
  }

  return children;
}
