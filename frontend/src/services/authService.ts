import { jwtDecode } from "jwt-decode";
import type { JwtPayload, LoginCredentials, LoginResult, RoleId } from "../types/auth.types";
import { ROLES, ROLE_MAP } from "../types/auth.types";

export const TOKEN_KEY = "fitmanager_token" as const;
const NOME_KEY = "fitmanager_nome" as const;

export const DASHBOARD_BY_ROLE: Record<RoleId, string> = {
  [ROLES.ADMIN]: "/admin-dashboard",
  [ROLES.INSTRUTOR]: "/instrutor-dashboard",
  [ROLES.ALUNO]: "/aluno-dashboard",
};

export { ROLES };

function notifyTokenChange(): void {
  window.dispatchEvent(new Event("fitmanager_token_changed"));
}

export function saveToken(token: string, nome: string): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(NOME_KEY, nome);  // persiste o nome junto
  notifyTokenChange();
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(NOME_KEY);
  notifyTokenChange();
}

export function getCurrentUser(): JwtPayload | null {
  const token = getToken();
  if (!token) return null;

  try {
    const decoded = jwtDecode<Record<string, unknown>>(token);

    const now = Date.now() / 1000;
    if (decoded.exp && (decoded.exp as number) < now) {
      removeToken();
      return null;
    }

    const perfil = decoded[
      "http://schemas.microsoft.com/ws/2008/06/identity/claims/role"
    ] as keyof typeof ROLE_MAP;

    const email = decoded[
      "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"
    ] as string;

    const id = Number(
      decoded[
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"
      ]
    );

    const nome = localStorage.getItem(NOME_KEY) ?? "";

    return {
      id,
      nome,
      email,
      perfil,
      id_cargo: ROLE_MAP[perfil],
      exp: decoded.exp as number | undefined,
      iat: decoded.iat as number | undefined,
    };
  } catch {
    removeToken();
    return null;
  }
}

// Prototipo sem backend: gera um token no formato JWT localmente, sem
// nenhuma chamada de rede nem validação de credenciais.
function buildFakeToken(perfil: keyof typeof ROLE_MAP, email: string): string {
  const header = { alg: "none", typ: "JWT" };
  const payload = {
    "http://schemas.microsoft.com/ws/2008/06/identity/claims/role": perfil,
    "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress": email,
    "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier": "1",
    exp: Math.floor(Date.now() / 1000) + 60 * 60 * 12,
    iat: Math.floor(Date.now() / 1000),
  };
  const encode = (obj: object): string =>
    btoa(JSON.stringify(obj)).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
  return `${encode(header)}.${encode(payload)}.fake-signature`;
}

function inferPerfil(email: string): keyof typeof ROLE_MAP {
  const lower = email.toLowerCase();
  if (lower.includes("instrutor")) return "INSTRUTOR";
  if (lower.includes("aluno")) return "ALUNO";
  return "ADMIN";
}

export async function login(credentials: LoginCredentials): Promise<LoginResult> {
  return fakeLogin(inferPerfil(credentials.email), credentials.email);
}

export function fakeLogin(perfil: keyof typeof ROLE_MAP, email?: string): LoginResult {
  const nome = `Usuário (${perfil})`;
  const resolvedEmail = email && email.trim() !== "" ? email : `${perfil.toLowerCase()}@prototipo.local`;
  const token = buildFakeToken(perfil, resolvedEmail);

  saveToken(token, nome);

  const user: JwtPayload = {
    id: 1,
    nome,
    email: resolvedEmail,
    perfil,
    id_cargo: ROLE_MAP[perfil],
  };

  const redirectTo = DASHBOARD_BY_ROLE[user.id_cargo] ?? "/";

  return { user, redirectTo };
}


export function logout(): void {
  removeToken();
  window.location.href = "/login";
}