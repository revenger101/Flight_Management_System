import { Link } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';

export default function Forbidden() {
  return (
    <div className="auth-loading-screen">
      <div>
        <ShieldAlert size={40} style={{ marginBottom: 12, color: 'var(--gold-light)' }} />
        <div style={{ fontSize: 28, marginBottom: 8 }}>Access Restricted</div>
        <div style={{ fontFamily: 'DM Sans, sans-serif', fontSize: 14, marginBottom: 16 }}>
          Your account role does not have access to this page.
        </div>
        <Link to="/">Return to dashboard</Link>
      </div>
    </div>
  );
}
