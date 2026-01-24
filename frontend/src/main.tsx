import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { Auth0Provider } from "@auth0/auth0-react";
import "./index.css";

import AppShell from "./layout/AppShell";
import Dashboard from "./features/dashboard/Dashboard";
import Trends from "./features/trends/Trends";
import Goals from "./features/goals/Goals";
import CheckIn from "./features/checkin/CheckIn";
import ProfilePage from "./features/profile/Profile";
import Settings from "./features/settings/Settings";

const router = createBrowserRouter([
  {
    element: <AppShell />,
    children: [
      { path: "/", element: <Dashboard /> },
      { path: "/trends", element: <Trends /> },
      { path: "/goals", element: <Goals /> },
      { path: "/checkin", element: <CheckIn /> },
      { path: "/profile", element: <ProfilePage /> },
      { path: "/settings", element: <Settings /> },

    ],
  },
]);

const domain = import.meta.env.VITE_AUTH0_DOMAIN as string;
const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID as string;
const audience = import.meta.env.VITE_AUTH0_AUDIENCE as string;

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience,
      }}
      cacheLocation="localstorage"
      useRefreshTokens={true}
    >
      <RouterProvider router={router} />
    </Auth0Provider>
  </React.StrictMode>
);
