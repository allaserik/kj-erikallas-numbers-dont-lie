import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
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

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
