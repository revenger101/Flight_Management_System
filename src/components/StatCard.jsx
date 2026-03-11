export default function StatCard({ icon, value, label, color = 'var(--gold)', bgColor = 'var(--gold-dim)' }) {
  return (
    <div className="stat-card" style={{ '--accent-color': bgColor }}>
      <div className="stat-icon" style={{ background: bgColor, color }}>
        {icon}
      </div>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}