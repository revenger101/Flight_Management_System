import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRight, Lock, Mail, PlaneTakeoff } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../services/authService';

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [form, setForm] = useState({ email: '', password: '' });
  const [isLoading, setIsLoading] = useState(false);

  const redirectTo = location.state?.from?.pathname || '/';

  const onChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await login(form);
      toast.success('Welcome back. You are now signed in.');
      navigate(redirectTo, { replace: true });
    } catch (error) {
      toast.error(error?.message || 'Unable to sign in. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const onGoogleLogin = () => {
    window.location.href = authService.getGoogleLoginUrl();
  };

  return (
    <div className="auth-page auth-login-page">
      <div className="auth-bg-layer" />
      <div className="auth-grid">
        <section className="auth-brand-card">
          <div className="auth-brand-badge">Control Tower Access</div>
          <h1>
            Ready for
            <span> Precision Operations</span>
          </h1>
          <p>
            Manage flights, passengers, booking workflows, and live operations in one
            premium control center.
          </p>
          <div className="auth-brand-metric">
            <PlaneTakeoff size={18} />
            <span>Secure aviation-grade session handling with JWT</span>
          </div>
        </section>

        <section className="auth-form-card">
          <h2>Sign In</h2>
          <p>Use your account credentials to continue.</p>

          <button type="button" className="auth-google-btn" onClick={onGoogleLogin}>
            <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
              <path fill="#EA4335" d="M12 10.2v3.9h5.5c-.2 1.2-1.4 3.6-5.5 3.6-3.3 0-6-2.8-6-6.2s2.7-6.2 6-6.2c1.9 0 3.2.8 4 1.5l2.7-2.6C17 2.6 14.7 1.6 12 1.6 6.9 1.6 2.8 5.8 2.8 11s4.1 9.4 9.2 9.4c5.3 0 8.8-3.7 8.8-8.9 0-.6-.1-1-.2-1.3z" />
            </svg>
            Continue with Google
          </button>

          <div className="auth-divider">or use email and password</div>

          <form className="auth-form" onSubmit={onSubmit}>
            <label>
              Email
              <div className="auth-input-wrap">
                <Mail size={16} />
                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={onChange}
                  required
                  placeholder="pilot@airport.com"
                />
              </div>
            </label>

            <label>
              Password
              <div className="auth-input-wrap">
                <Lock size={16} />
                <input
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={onChange}
                  required
                  placeholder="********"
                />
              </div>
            </label>

            <button className="auth-submit-btn" type="submit" disabled={isLoading}>
              {isLoading ? 'Signing in...' : 'Sign In'}
              {!isLoading && <ArrowRight size={16} />}
            </button>
          </form>

          <div className="auth-footer-note">
            New here? <Link to="/signup">Create your professional account</Link>
          </div>
        </section>
      </div>
    </div>
  );
}
