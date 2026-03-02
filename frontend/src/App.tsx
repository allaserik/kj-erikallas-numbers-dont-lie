import './App.css';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppShell from './layout/AppShell';
import DashboardPage from './features/dashboard/DashboardPage';
import TrendsPage from './features/trends/TrendsPage';
import GoalsPage from './features/goals/GoalsPage';
import CheckInPage from './features/checkin/CheckInPage';
import ProfilePage from './features/profile/ProfilePage';
import { VerifyEmailPage } from './features/auth/VerifyEmailPage';
import { useAuth0 } from '@auth0/auth0-react';
import { LoginModal } from './features/auth/LoginModal';
import { SplashScreen } from './shared/ui/SplashScreen';
import { useLocalAuth } from './shared/auth/useLocalAuth';
import { useEffect, useState } from 'react';


// App.tsx sets up routing and uses AppShell as a layout route.
// Email verification is public (accessible without auth)
// Unauthenticated users see LoginModal when accessing protected routes
function App() {
  const { isAuthenticated: auth0Authenticated, isLoading } = useAuth0();
  const { isAuthenticated: localAuthenticated } = useLocalAuth();
  const [isAuthenticated, setIsAuthenticated] = useState(auth0Authenticated || localAuthenticated);

  // Listen for local auth changes (login/logout)
  useEffect(() => {
    const handleAuthChange = () => {
      setIsAuthenticated(auth0Authenticated || localAuthenticated);
    };

    window.addEventListener('localAuthChanged', handleAuthChange);
    return () => window.removeEventListener('localAuthChanged', handleAuthChange);
  }, [auth0Authenticated, localAuthenticated]);

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
