export default function PaginationControls({
  page,
  totalPages,
  pageSize,
  totalItems,
  rangeStart,
  rangeEnd,
  onPageChange,
  onPageSizeChange,
}) {
  return (
    <div className="pagination-wrap">
      <div className="pagination-left">
        <span>
          Showing <strong>{rangeStart}</strong>-<strong>{rangeEnd}</strong> of <strong>{totalItems}</strong>
        </span>
      </div>
      <div className="pagination-right">
        <label className="pagination-size">
          Rows
          <select value={pageSize} onChange={(e) => onPageSizeChange(Number(e.target.value))}>
            {[5, 10, 20, 50].map((size) => (
              <option key={size} value={size}>{size}</option>
            ))}
          </select>
        </label>
        <button className="btn btn-secondary btn-sm" disabled={page === 1} onClick={() => onPageChange(page - 1)}>
          Prev
        </button>
        <span className="pagination-page">Page {page} / {totalPages}</span>
        <button className="btn btn-secondary btn-sm" disabled={page === totalPages} onClick={() => onPageChange(page + 1)}>
          Next
        </button>
      </div>
    </div>
  );
}
