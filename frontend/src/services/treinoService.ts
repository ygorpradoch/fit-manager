import { getToken } from "./authService";

const API_URL = import.meta.env.VITE_API_URL as string;

export interface Exercicio {
  id?: number;
  nome: string;
  series: number;
  repeticoes: number;
  descanso: number;   
  carga: number;      
  observacoes: string;
}

export interface Sessao {
  id?: number;
  nome: string;
  exercicios: Exercicio[];
}

export interface Treino {
  id: number;
  nome: string;
  objetivo: string;
  observacoesGerais: string;
  status: string;
}


export interface RelatorioTreino {
  treinoId: number;
  nomeTreino: string;
  instrutor: string;
  aluno: string;
  quantidadeSessoes: number;
  quantidadeExercicios: number;
  tempoEstimado: string;
  status: string;
  createdAt: string;
  sessoes?: Sessao[];
}


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

export async function getSessoesDeTreino(treinoId: number): Promise<Sessao[]> {
  const data = await apiFetch<{ treinoId: number; sessoes: Sessao[] }>(
    `/api/v1/treinos/${treinoId}/sessoes`
  );
  return (data.sessoes ?? []).map((s) => ({
    ...s,
    exercicios: s.exercicios ?? [],
  }));
}

export async function criarSessao(
  treinoId: number,
  nome: string,
  grupoMuscular: string
): Promise<Sessao> {
  const data = await apiFetch<{ message: string; sessao: { id: number } }>(
    `/api/v1/treinos/${treinoId}/sessoes`,
    {
      method: "POST",
      body: JSON.stringify({ nomeSessao: nome, grupoMuscular }),
    }
  );
  return { id: data.sessao.id, nome, exercicios: [] };
}

export function deletarSessao(sessaoId: number): Promise<null> {
  return apiFetch<null>(`/api/v1/sessoes/${sessaoId}`, { method: "DELETE" });
}


export function criarExercicio(
  sessaoId: number,
  payload: Omit<Exercicio, "id">
): Promise<Exercicio> {
  return apiFetch<Exercicio>(`/api/v1/sessoes/${sessaoId}/exercicios`, {
    method: "POST",
    body: JSON.stringify({
      nomeExercicio: payload.nome,
      series: payload.series,
      repeticoes: payload.repeticoes,
      carga: payload.carga,
      descanso: payload.descanso,
      observacoes: payload.observacoes,
    }),
  });
}

export function atualizarExercicio(
  exercicioId: number,
  payload: Omit<Exercicio, "id">
): Promise<Exercicio> {
  return apiFetch<Exercicio>(`/api/v1/exercicios/${exercicioId}`, {
    method: "PUT",
    body: JSON.stringify({
      nomeExercicio: payload.nome,
      series: payload.series,
      repeticoes: payload.repeticoes,
      carga: payload.carga,
      descanso: payload.descanso,
      observacoes: payload.observacoes,
    }),
  });
}

export function deletarExercicio(exercicioId: number): Promise<null> {
  return apiFetch<null>(`/api/v1/exercicios/${exercicioId}`, { method: "DELETE" });
}


export async function getRelatorio(treinoId: number): Promise<RelatorioTreino> {
  return apiFetch<RelatorioTreino>(`/api/v1/treinos/${treinoId}/relatorio`);
}

export function getTreino(treinoId: number): Promise<Treino> {
  return apiFetch<Treino>(`/api/v1/treinos/${treinoId}`);
}