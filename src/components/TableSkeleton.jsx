export default function TableSkeleton({ rows = 8, cols = 6 }) {
  return (
    <div className="table-skeleton" role="status" aria-label="Loading data">
      {Array.from({ length: rows }).map((_, rowIdx) => (
        <div className="table-skeleton-row" key={`skeleton-row-${rowIdx}`}>
          {Array.from({ length: cols }).map((__, colIdx) => (
            <span className="table-skeleton-cell" key={`skeleton-cell-${rowIdx}-${colIdx}`} />
          ))}
        </div>
      ))}
    </div>
  );
}
