import Sidebar from './Sidebar';
import Navbar from './Navbar';
import { Toaster } from 'react-hot-toast';

export default function Layout({ children, title, subtitle }) {
  return (
    <div className="layout">
      <Sidebar />
      <div className="main-content">
        <Navbar title={title} subtitle={subtitle} />
        <div className="page-content">{children}</div>
      </div>
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: '#1a2438',
            color: '#f0f4ff',
            border: '1px solid #1e2d45',
            borderRadius: '10px',
            fontSize: '14px',
          },
          success: { iconTheme: { primary: '#c9a84c', secondary: '#1a2438' } },
        }}
      />
    </div>
  );
}