import { useState, useEffect, useCallback, useMemo } from "react";
import {
  getCurrentUser,
  logout as authLogout,
  ROLES,
  DASHBOARD_BY_ROLE,
  TOKEN_KEY,
} from "../services/authService";
import type { JwtPayload, RoleId } from "../types/auth.types";

interface UseAuthReturn {
  user: JwtPayload | null;
  isAuth: boolean;
  role: RoleId | null;
  isAdmin: boolean;
  isInstrutor: boolean;
  isAluno: boolean;
  dashboardPath: string;
  logout: () => void;
  refresh: () => void;
}

export function useAuth(): UseAuthReturn {
  const [token, setToken] = useState<string | null>(
    () => localStorage.getItem(TOKEN_KEY)
  );

  const refresh = useCallback((): void => {
    setToken(localStorage.getItem(TOKEN_KEY));
  }, []);

  useEffect(() => {
    const onTokenChanged = (): void => {
      setToken(localStorage.getItem(TOKEN_KEY));
    };

    const onStorage = (e: StorageEvent): void => {
      if (e.key === TOKEN_KEY) setToken(e.newValue);
    };

    window.addEventListener("fitmanager_token_changed", onTokenChanged);
    window.addEventListener("storage", onStorage);
    return () => {
      window.removeEventListener("fitmanager_token_changed", onTokenChanged);
      window.removeEventListener("storage", onStorage);
    };
  }, []);

  const user = useMemo(() => {
    if (!token) return null;

    const decoded = getCurrentUser();

    return decoded;
  }, [token]);

  const role: RoleId | null = user?.id_cargo ?? null;

  const dashboardPath =
    role !== null
      ? (DASHBOARD_BY_ROLE[role] ?? "/")
      : "/";

  return {
    user,
    isAuth: user !== null,
    role,
    isAdmin: role === ROLES.ADMIN,
    isInstrutor: role === ROLES.INSTRUTOR,
    isAluno: role === ROLES.ALUNO,
    dashboardPath,
    logout: authLogout,
    refresh,
  };
}