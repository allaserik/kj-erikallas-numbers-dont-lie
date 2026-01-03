import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { Auth0Provider } from "@auth0/auth0-react";
import "./index.css";

import AppShell from "./layout/AppShell";
import Dashboard from "./pages/Dashboard";
import Trends from "./pages/Trends";
import Goals from "./pages/Goals";
import CheckIn from "./pages/CheckIn";

const router = createBrowserRouter([
  {
    element: <AppShell />,
    children: [
      { path: "/", element: <Dashboard /> },
      { path: "/trends", element: <Trends /> },
      { path: "/goals", element: <Goals /> },
      { path: "/checkin", element: <CheckIn /> },
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
