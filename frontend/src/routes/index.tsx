import { createBrowserRouter, Navigate } from "react-router-dom";
import { PrivateRoute } from "../components/auth/PrivateRoutes";
import { ROLES } from "../services/authService";
import { LoginPage }          from "../pages/auth/Login";
import  DashboardGestor  from "../pages/gestor/Dashboard";
import { Dashboard } from "../pages/instrutor/Dashboard";
import  DashboardAluno      from "../pages/aluno/Dashboard";
import MeuTreino from "../pages/aluno/MeuTreino";
import { NotFound }           from "../pages/notFound/notFound";
import { GerenciarTreino } from "../pages/instrutor/treinos/GerenciarTreino";

export const router = createBrowserRouter([
  {
    path: "/login",
    element: <LoginPage />,
  },

  {
    path: "/",
    element: <Navigate to="/login" replace />,
  },

  {
    element: <PrivateRoute allowedRoles={[ROLES.ADMIN]} />,
    children: [
      { path: "/admin-dashboard", element: <DashboardGestor /> },
    ],
  },

  {
    element: <PrivateRoute allowedRoles={[ROLES.INSTRUTOR]} />,
    children: [
      {
        path: "/instrutor-dashboard",
        element: <Dashboard />,
      },
      {
        path: "/treinos/:treinoId/editar",
        element: <GerenciarTreino />,
      },
    ],
  },

  {
    element: <PrivateRoute allowedRoles={[ROLES.ALUNO]} />,
    children: [
      { path: "/aluno-dashboard", element: <DashboardAluno /> },
      { path: "/meu-treino", element: <MeuTreino /> },
    ],
  },

  {
    path: "*",
    element: <NotFound />,
  },
]);