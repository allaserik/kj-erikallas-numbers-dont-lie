import './App.css';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppShell from './layout/AppShell';
import DashboardPage from './features/dashboard/DashboardPage';
import TrendsPage from './features/trends/TrendsPage';
import GoalsPage from './features/goals/GoalsPage';
import CheckInPage from './features/checkin/CheckInPage';
import ProfilePage from './features/profile/ProfilePage';
import { VerifyEmailPage } from './features/auth/VerifyEmailPage';
import { LoginModal } from './features/auth/LoginModal';
import { SplashScreen } from './shared/ui/SplashScreen';
import { useAppAuth } from './shared/auth/AuthContext';

// App: Uses centralized AuthContext from AuthProvider
// All auth checks happen in AuthContext, App just consumes unified state
function App() {
  const { isAuthenticated, isLoading } = useAppAuth();

  // Show splash screen while loading auth state
  if (isLoading) return <SplashScreen />;

  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes - no auth required */}
        <Route path="/verify-email" element={<VerifyEmailPage />} />

        {/* Protected routes - require auth */}
        <Route path="/" element={<AppShell />}>
          <Route index element={<DashboardPage />} />
          <Route path="trends" element={<TrendsPage />} />
          <Route path="goals" element={<GoalsPage />} />
          <Route path="checkin" element={<CheckInPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Routes>

      {/* Show login modal if trying to access protected routes without auth */}
      {!isAuthenticated && <LoginModal />}
    </BrowserRouter>
  );
}

export default App;
