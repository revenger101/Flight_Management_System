import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowRight, Lock, Mail, UserRound } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../contexts/AuthContext';

export default function Signup() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [isLoading, setIsLoading] = useState(false);

  const onChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();

    if (form.password !== form.confirmPassword) {
      toast.error('Passwords do not match.');
      return;
    }

    setIsLoading(true);
    try {
      await register({
        fullName: form.fullName,
        email: form.email,
        password: form.password,
      });
      toast.success('Account created successfully. Welcome aboard.');
      navigate('/', { replace: true });
    } catch (error) {
      toast.error(error?.message || 'Unable to create account.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page auth-signup-page">
      <div className="auth-bg-layer" />
      <div className="auth-grid">
        <section className="auth-brand-card">
          <div className="auth-brand-badge">Launch New Account</div>
          <h1>
            Build your
            <span> Operations Workspace</span>
          </h1>
          <p>
            Access analytics, booking controls, and passenger lifecycle tools from a
            secure, enterprise-ready dashboard.
          </p>
          <div className="auth-brand-metric">
            <UserRound size={18} />
            <span>Fast onboarding with secure backend validation</span>
          </div>
        </section>

        <section className="auth-form-card">
          <h2>Create Account</h2>
          <p>Set up your secure access in under a minute.</p>

          <form className="auth-form" onSubmit={onSubmit}>
            <label>
              Full Name
              <div className="auth-input-wrap">
                <UserRound size={16} />
                <input
                  type="text"
                  name="fullName"
                  value={form.fullName}
                  onChange={onChange}
                  required
                  placeholder="Alex Morgan"
                />
              </div>
            </label>

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
                  placeholder="ops@airport.com"
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
                  minLength={8}
                  placeholder="At least 8 characters"
                />
              </div>
            </label>

            <label>
              Confirm Password
              <div className="auth-input-wrap">
                <Lock size={16} />
                <input
                  type="password"
                  name="confirmPassword"
                  value={form.confirmPassword}
                  onChange={onChange}
                  required
                  minLength={8}
                  placeholder="Repeat your password"
                />
              </div>
            </label>

            <button className="auth-submit-btn" type="submit" disabled={isLoading}>
              {isLoading ? 'Creating account...' : 'Create Account'}
              {!isLoading && <ArrowRight size={16} />}
            </button>
          </form>

          <div className="auth-footer-note">
            Already have an account? <Link to="/login">Sign in now</Link>
          </div>
        </section>
      </div>
    </div>
  );
}
