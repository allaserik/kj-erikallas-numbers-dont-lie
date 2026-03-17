import './App.css';
import type { ReactElement } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import AppShell from './layout/AppShell';
import DashboardPage from './features/dashboard/DashboardPage';
import TrendsPage from './features/trends/TrendsPage';
import GoalsPage from './features/goals/GoalsPage';
import CheckInPage from './features/checkin/CheckInPage';
import ProfilePage from './features/profile/ProfilePage';
import SettingsPage from './features/settings/SettingsPage';
import ConsentPage from './features/consent/ConsentPage';
import NutritionPreferencesPage from './features/nutrition/NutritionPreferencesPage';
import PlannerPage from './features/planner/PlannerPage';
import { VerifyEmailPage } from './features/auth/VerifyEmailPage';
import { ForgotPasswordPage } from './features/auth/ForgotPasswordPage';
import { ResetPasswordPage } from './features/auth/ResetPasswordPage';
import { LoginModal } from './features/auth/LoginModal';
import { SplashScreen } from './shared/ui/SplashScreen';
import { useAppAuth } from './shared/auth/AuthContext';
import { useAuthedQuery } from './shared/auth/useAuthedQuery';
import { getPrivacyPreferences } from './shared/api/privacy';

function AppContent({ isAuthenticated }: { isAuthenticated: boolean }) {
  const location = useLocation();
  const publicPaths = new Set(['/verify-email', '/forgot-password', '/reset-password']);
  const isPublicRoute = publicPaths.has(location.pathname);
  const privacyQ = useAuthedQuery(`consentGate:${location.pathname}`, getPrivacyPreferences, isAuthenticated);
  const consentLoading = isAuthenticated && privacyQ.loading;
  const hasConsent = !!privacyQ.data?.data_usage_consent;
  const consentRequired = isAuthenticated && !consentLoading && !privacyQ.error && !hasConsent;

  const guarded = (element: ReactElement) =>
    consentRequired ? <Navigate to="/consent" replace /> : element;

  if (consentLoading && !isPublicRoute) {
    return <SplashScreen />;
  }

  return (
    <>
      <Routes>
        {/* Public routes - no auth required */}
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        {/* Protected routes - require auth */}
        <Route path="/" element={<AppShell />}>
          <Route index element={guarded(<DashboardPage />)} />
          <Route path="planner" element={guarded(<PlannerPage />)}>
            <Route index element={<Navigate to="/planner/today" replace />} />
            <Route path="today" element={<CheckInPage />} />
            <Route path="goals" element={<GoalsPage />} />
            <Route path="nutrition" element={<NutritionPreferencesPage />} />
          </Route>
          <Route path="trends" element={guarded(<TrendsPage />)} />
          <Route path="goals" element={<Navigate to="/planner/goals" replace />} />
          <Route path="checkin" element={<Navigate to="/planner/today" replace />} />
          <Route path="profile" element={guarded(<ProfilePage />)} />
          <Route path="settings" element={guarded(<SettingsPage />)} />
          <Route path="nutrition/preferences" element={<Navigate to="/planner/nutrition" replace />} />
          <Route path="consent" element={hasConsent ? <Navigate to="/" replace /> : <ConsentPage />} />
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
