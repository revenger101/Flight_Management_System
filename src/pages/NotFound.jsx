import { Link } from 'react-router-dom';
import { Compass } from 'lucide-react';

export default function NotFound() {
  return (
    <div className="app-fallback">
      <Compass size={34} />
      <h1>404 - Page not found</h1>
      <p>The route you requested does not exist in this portal.</p>
      <Link className="btn btn-primary" to="/">Go to Dashboard</Link>
    </div>
  );
}
