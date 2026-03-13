import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import html2canvas from 'html2canvas';

export function exportToCsv(filename, rows) {
  if (!rows || rows.length === 0) return;

  const headers = Object.keys(rows[0]);
  const escapeCell = (value) => {
    const str = String(value ?? '');
    if (str.includes(',') || str.includes('"') || str.includes('\n')) {
      return `"${str.replace(/"/g, '""')}"`;
    }
    return str;
  };

  const csv = [
    headers.join(','),
    ...rows.map((row) => headers.map((h) => escapeCell(row[h])).join(',')),
  ].join('\n');

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${filename}.csv`;
  link.click();
  URL.revokeObjectURL(url);
}

export function exportToExcel(filename, rows, sheetName = 'Data') {
  if (!rows || rows.length === 0) return;
  const worksheet = XLSX.utils.json_to_sheet(rows);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName);
  XLSX.writeFile(workbook, `${filename}.xlsx`);
}

export function exportTablePdf(filename, title, rows) {
  if (!rows || rows.length === 0) return;

  const doc = new jsPDF('p', 'pt', 'a4');
  const pageWidth = doc.internal.pageSize.getWidth();

  doc.setFillColor(13, 20, 32);
  doc.rect(0, 0, pageWidth, 92, 'F');

  doc.setTextColor(232, 201, 106);
  doc.setFontSize(22);
  doc.text(title, 40, 44);

  doc.setTextColor(136, 153, 187);
  doc.setFontSize(11);
  doc.text(`Generated: ${new Date().toLocaleString()}`, 40, 68);

  const headers = Object.keys(rows[0]);
  const body = rows.map((r) => headers.map((h) => String(r[h] ?? '')));

  autoTable(doc, {
    head: [headers],
    body,
    startY: 110,
    theme: 'grid',
    styles: {
      fontSize: 9,
      cellPadding: 6,
      textColor: [30, 41, 59],
    },
    headStyles: {
      fillColor: [17, 24, 39],
      textColor: [248, 250, 252],
      fontStyle: 'bold',
    },
    alternateRowStyles: {
      fillColor: [247, 249, 252],
    },
  });

  doc.save(`${filename}.pdf`);
}

export async function exportDashboardSnapshotPdf(filename, dashboardElement, metrics = {}) {
  if (!dashboardElement) return;

  const canvas = await html2canvas(dashboardElement, { scale: 2, useCORS: true, backgroundColor: '#080c14' });
  const imageData = canvas.toDataURL('image/png');

  const doc = new jsPDF('p', 'pt', 'a4');
  const pageWidth = doc.internal.pageSize.getWidth();

  doc.setFillColor(10, 16, 28);
  doc.rect(0, 0, pageWidth, 108, 'F');

  doc.setTextColor(232, 201, 106);
  doc.setFontSize(24);
  doc.text('AirPort Analytics Report', 40, 46);

  doc.setTextColor(152, 166, 189);
  doc.setFontSize(11);
  doc.text(`Generated on ${new Date().toLocaleString()}`, 40, 70);

  const chips = [
    `Flights: ${metrics.flights ?? '-'}`,
    `Bookings: ${metrics.bookings ?? '-'}`,
    `Passengers: ${metrics.passengers ?? '-'}`,
  ];

  let chipX = 40;
  chips.forEach((chip) => {
    doc.setFillColor(17, 28, 45);
    doc.roundedRect(chipX, 78, 115, 22, 6, 6, 'F');
    doc.setTextColor(232, 240, 255);
    doc.setFontSize(10);
    doc.text(chip, chipX + 8, 93);
    chipX += 122;
  });

  const imageWidth = pageWidth - 60;
  const imageHeight = (canvas.height * imageWidth) / canvas.width;
  const maxHeight = doc.internal.pageSize.getHeight() - 150;
  const finalHeight = Math.min(imageHeight, maxHeight);

  doc.addImage(imageData, 'PNG', 30, 124, imageWidth, finalHeight);
  doc.save(`${filename}.pdf`);
}
