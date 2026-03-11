export default function Navbar({ title, subtitle }) {
  const now = new Date();
  const time = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  const date = now.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });

  return (
    <header className="navbar">
      <div className="navbar-left">
        <h2>{title}</h2>
        {subtitle && <p>{subtitle}</p>}
      </div>
      <div className="navbar-right">
        <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>{date} · {time}</span>
      </div>
    </header>
  );
}