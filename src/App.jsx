import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import Airlines from './pages/Airlines';
import Airports from './pages/Airports';
import Flights from './pages/Flights';
import Passengers from './pages/Passengers';
import Bookings from './pages/Bookings';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/airlines" element={<Airlines />} />
        <Route path="/airports" element={<Airports />} />
        <Route path="/flights" element={<Flights />} />
        <Route path="/passengers" element={<Passengers />} />
        <Route path="/bookings" element={<Bookings />} />
      </Routes>
    </BrowserRouter>
  );
}