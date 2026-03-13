import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function PublicOnlyRoute({ children }) {
  const { isReady, isAuthenticated } = useAuth();

  if (!isReady) {
    return <div className="auth-loading-screen">Preparing authentication...</div>;
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return children;
}
