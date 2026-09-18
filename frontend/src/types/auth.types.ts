export const ROLES = {
  ADMIN: 1,
  INSTRUTOR: 2,
  ALUNO: 3,
} as const;

export const ROLE_MAP = {
  ADMIN: ROLES.ADMIN,
  INSTRUTOR: ROLES.INSTRUTOR,
  ALUNO: ROLES.ALUNO,
} as const;

export type RoleId = (typeof ROLES)[keyof typeof ROLES];

export interface JwtPayload {
  id: number;
  nome: string;
  email: string;
  perfil: keyof typeof ROLES;
  id_cargo: RoleId;
  exp?: number;
  iat?: number;
}

export interface LoginApiResponse {
  token: string;
  perfil: keyof typeof ROLES;
  id: number;
  nomeCompleto: string;
}

export interface LoginResult {
  user: JwtPayload;
  redirectTo: string;
}

export interface LoginCredentials {
  email: string;
  senha: string;
}