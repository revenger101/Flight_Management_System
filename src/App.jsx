import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Dashboard from './pages/Dashboard';
import Airlines from './pages/Airlines';
import Airports from './pages/Airports';
import Flights from './pages/Flights';
import Passengers from './pages/Passengers';
import Bookings from './pages/Bookings';
import FlightDetails from './pages/FlightDetails';
import PassengerDetails from './pages/PassengerDetails';
import Login from './pages/Login';
import Signup from './pages/Signup';
import OAuth2Success from './pages/OAuth2Success';
import Forbidden from './pages/Forbidden';
import ErrorBoundary from './components/ErrorBoundary';
import ProtectedRoute from './components/ProtectedRoute';
import PublicOnlyRoute from './components/PublicOnlyRoute';
import RoleProtectedRoute from './components/RoleProtectedRoute';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route
              path="/login"
              element={(
                <PublicOnlyRoute>
                  <Login />
                </PublicOnlyRoute>
              )}
            />
            <Route
              path="/signup"
              element={(
                <PublicOnlyRoute>
                  <Signup />
                </PublicOnlyRoute>
              )}
            />
            <Route path="/oauth2/success" element={<OAuth2Success />} />
            <Route path="/forbidden" element={<Forbidden />} />

            <Route
              path="/"
              element={(
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              )}
            />
            <Route
              path="/airlines"
              element={(
                <RoleProtectedRoute allowedRoles={['ADMIN']}>
                  <Airlines />
                </RoleProtectedRoute>
              )}
            />
            <Route
              path="/airports"
              element={(
                <RoleProtectedRoute allowedRoles={['ADMIN']}>
                  <Airports />
                </RoleProtectedRoute>
              )}
            />
            <Route
              path="/flights"
              element={(
                <ProtectedRoute>
                  <Flights />
                </ProtectedRoute>
              )}
            />
            <Route
              path="/flights/:id"
              element={(
                <ProtectedRoute>
                  <FlightDetails />
                </ProtectedRoute>
              )}
            />
            <Route
              path="/passengers"
              element={(
                <ProtectedRoute>
                  <Passengers />
                </ProtectedRoute>
              )}
            />
            <Route
              path="/passengers/:id"
              element={(
                <ProtectedRoute>
                  <PassengerDetails />
                </ProtectedRoute>
              )}
            />
            <Route
              path="/bookings"
              element={(
                <ProtectedRoute>
                  <Bookings />
                </ProtectedRoute>
              )}
            />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ErrorBoundary>
  );
}