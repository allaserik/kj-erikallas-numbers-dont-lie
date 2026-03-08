import './App.css';
import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import AppShell from './layout/AppShell';
import DashboardPage from './features/dashboard/DashboardPage';
import TrendsPage from './features/trends/TrendsPage';
import GoalsPage from './features/goals/GoalsPage';
import CheckInPage from './features/checkin/CheckInPage';
import ProfilePage from './features/profile/ProfilePage';
import { VerifyEmailPage } from './features/auth/VerifyEmailPage';
import { ForgotPasswordPage } from './features/auth/ForgotPasswordPage';
import { ResetPasswordPage } from './features/auth/ResetPasswordPage';
import { LoginModal } from './features/auth/LoginModal';
import { SplashScreen } from './shared/ui/SplashScreen';
import { useAppAuth } from './shared/auth/AuthContext';

function AppContent({ isAuthenticated }: { isAuthenticated: boolean }) {
  const location = useLocation();
  const publicPaths = new Set(['/verify-email', '/forgot-password', '/reset-password']);
  const isPublicRoute = publicPaths.has(location.pathname);

  return (
    <>
      <Routes>
        {/* Public routes - no auth required */}
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        {/* Protected routes - require auth */}
        <Route path="/" element={<AppShell />}>
          <Route index element={<DashboardPage />} />
          <Route path="trends" element={<TrendsPage />} />
          <Route path="goals" element={<GoalsPage />} />
          <Route path="checkin" element={<CheckInPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Routes>

      {/* Show login modal on protected routes when not authenticated */}
      {!isAuthenticated && !isPublicRoute && <LoginModal />}
    </>
  );
}

// App: Uses centralized AuthContext from AuthProvider
// All auth checks happen in AuthContext, App just consumes unified state
function App() {
  const { isAuthenticated, isLoading } = useAppAuth();

  // Show splash screen while loading auth state
  if (isLoading) return <SplashScreen />;

  return (
    <BrowserRouter>
      <AppContent isAuthenticated={isAuthenticated} />
    </BrowserRouter>
  );
}

export default App;
