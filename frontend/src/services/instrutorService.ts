import { API_URL } from "../services/api";
import { getToken } from "./authService";
import type { Aluno } from "../services/alunoService";
import type { TreinoCompleto } from "./treinoService";

function authHeaders(): HeadersInit {
  const token = getToken();
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: authHeaders(),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({})) as { message?: string };
    throw new Error(error.message ?? `Erro ${response.status}`);
  }

  if (response.status === 204) return null as T;

  return response.json() as Promise<T>;
}

export async function getAlunosDoInstrutor(instrutorId: number): Promise<Aluno[]> {
  const result = await apiFetch<{ items: Aluno[] }>(
    `/api/v1/instrutores/${instrutorId}/alunos`
  );
  return result.items;
}

export async function getTreinosDoInstrutor(instrutorId: number): Promise<TreinoCompleto[]> {
  const result = await apiFetch<{ total: number; page: number; items: TreinoCompleto[] }>(
    `/api/v1/instrutores/${instrutorId}/treinos`
  );
  return result.items;
}


export async function getTodosAlunos(): Promise<Aluno[]> {
  const result = await apiFetch<{ total: number; page: number; items: Aluno[] }>(
    "/api/v1/alunos"
  );
  return result.items;
}

export function deletarTreino(treinoId: number): Promise<null> {
  return apiFetch<null>(`/api/v1/treinos/${treinoId}`, { method: "DELETE" });
}

export function criarTreino(payload: {
    alunoId: number;
    instrutorId: number;
    nome: string;
    objetivo?: string;
    observacoesGerais?: string;
  } ) {
    return apiFetch<any>("/api/v1/treinos", {
      method: "POST",
      body: JSON.stringify(payload),
    }
  );
}