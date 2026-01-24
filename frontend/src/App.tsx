import './App.css';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppShell from './layout/AppShell';
import DashboardPage from './features/dashboard/DashboardPage';
import TrendsPage from './features/trends/TrendsPage';
import GoalsPage from './features/goals/GoalsPage';
import CheckInPage from './features/checkin/CheckInPage';
import ProfilePage from './features/profile/ProfilePage';


// App.tsx sets up routing and uses AppShell as a layout route.
// All main pages are nested as children of AppShell, which renders <Outlet />.
function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* AppShell is the layout route; all pages are nested here */}
        <Route path="/" element={<AppShell />}>
          {/* index means the default route for this layout */}
          <Route index element={<DashboardPage />} />
          <Route path="trends" element={<TrendsPage />} />
          <Route path="goals" element={<GoalsPage />} />
          <Route path="checkin" element={<CheckInPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
