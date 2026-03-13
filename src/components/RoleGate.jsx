import { useAuth } from '../contexts/AuthContext';

export default function RoleGate({ allowedRoles, fallback = null, children }) {
  const { user } = useAuth();
  if (!allowedRoles?.includes(user?.role)) {
    return fallback;
  }
  return children;
}
