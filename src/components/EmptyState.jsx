export default function EmptyState({ icon, title, description, ctaLabel, onCta }) {
  return (
    <div className="empty-state">
      {icon}
      <p className="empty-state-title">{title}</p>
      <p className="empty-state-desc">{description}</p>
      {ctaLabel && onCta && (
        <button className="btn btn-primary btn-sm" onClick={onCta}>{ctaLabel}</button>
      )}
    </div>
  );
}
