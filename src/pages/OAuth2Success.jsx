import { useEffect, useMemo } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../contexts/AuthContext';

export default function OAuth2Success() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { completeOAuthLogin } = useAuth();

  const token = useMemo(() => searchParams.get('token'), [searchParams]);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      if (!token) {
        toast.error('OAuth2 callback is missing token.');
        navigate('/login', { replace: true });
        return;
      }

      try {
        await completeOAuthLogin(token);
        if (!cancelled) {
          toast.success('Google sign-in completed successfully.');
          navigate('/', { replace: true });
        }
      } catch {
        if (!cancelled) {
          toast.error('Unable to complete Google sign-in.');
          navigate('/login', { replace: true });
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [completeOAuthLogin, navigate, token]);

  return (
    <div className="auth-loading-screen">
      Finalizing your secure Google session...<br />
      <Link to="/login">Return to login</Link>
    </div>
  );
}
