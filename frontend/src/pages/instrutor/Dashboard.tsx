import { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

import {
  getAlunosDoInstrutor,
  getTodosAlunos,
  getTreinosDoInstrutor,
  deletarTreino,
  criarTreino
} from "../../services/instrutorService";

import type { Aluno } from "../../services/alunoService";
import type { Treino } from "../../services/treinoService";
import type { TreinoCompleto } from "../../services/treinoService";

import { HeaderInstrutor } from "../instrutor/components/HeaderInstrutor";
import { AlunosVinculadosCard } from "../instrutor/components/AlunosVinculadosTable";
import { AlunosDisponiveisCard } from "../instrutor/components/AlunosDisponiveisTable";

import "./Dashboard.css";

export function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const instrutorId = user?.id ?? 0;

  const [alunosVinculados, setAlunosVinculados] = useState<Aluno[]>([]);
  const [treinos, setTreinos] = useState<TreinoCompleto[]>([]);
  const [todosAlunos, setTodosAlunos] = useState<Aluno[]>([]);

  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState("");

  const [modalNovoTreino, setModalNovoTreino] = useState(false);
  const [alunoParaVincular, setAlunoParaVincular] = useState<Aluno | null>(null);
  const [novoTreino, setNovoTreino] = useState({
    nome: "",
    objetivo: "",
    observacoesGerais: "",
  });

  const [buscaVinculados, setBuscaVinculados] = useState("");
  const [buscaDisponiveis, setBuscaDisponiveis] = useState("");

  async function carregarDados() {
    setLoading(true);
    

    try {
      const [vinculados, treinosData, todos] =
        await Promise.all([
          getAlunosDoInstrutor(instrutorId),
          getTreinosDoInstrutor(instrutorId),
          getTodosAlunos(),
        ]);

      setAlunosVinculados(vinculados);
      setTreinos(treinosData);
      setTodosAlunos(todos);
    } catch (e) {
      setErro(
        e instanceof Error
          ? e.message
          : "Erro ao carregar dados."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    carregarDados();
  }, [instrutorId]);

  const idsVinculados = useMemo(
    () => new Set(alunosVinculados.map((a) => a.id)),
    [alunosVinculados]
  );

  const alunosDisponiveis = useMemo(
    () =>
      todosAlunos.filter(
        (a) => !idsVinculados.has(a.id)
      ),
    [todosAlunos, idsVinculados]
  );

  function filtrar(lista: Aluno[], termo: string) {
    const t = termo.toLowerCase().trim();

    if (!t) return lista;

    return lista.filter(
      (a) =>
        a.nomeCompleto
          .toLowerCase()
          .includes(t) ||
        a.matricula
          ?.toLowerCase()
          .includes(t)
    );
  }

  const vinculadosFiltrados = filtrar(
    alunosVinculados,
    buscaVinculados
  );

  const disponiveisFiltrados = filtrar(
    alunosDisponiveis,
    buscaDisponiveis
  );

  async function handleDeletar(aluno: Aluno) {
    const treino = treinos.find((t) => t.alunoId === aluno.id);
    if (!treino) { alert("Treino não encontrado."); return; }
    if (!confirm(`Remover vínculo com ${aluno.nomeCompleto}?`)) return;
    try {
      await deletarTreino(treino.treinoId);
      await carregarDados();
    } catch (error) { console.error(error); }
  }

  function handleEditarTreino(aluno: Aluno) {
    const treino = treinos.find((t) => t.alunoId === aluno.id); 
    if (!treino) return;
    navigate(`/treinos/${treino.treinoId}/editar`); 
  }

  function handleAbrirModalTreino(aluno: Aluno) {
    setAlunoParaVincular(aluno);
    setNovoTreino({ nome: "", objetivo: "", observacoesGerais: "" });
    setModalNovoTreino(true);
  }

  async function handleConfirmarVincular() {
    if (!alunoParaVincular || !novoTreino.nome.trim()) return;
      const data = await criarTreino({
       alunoId: alunoParaVincular.id,
        instrutorId,
        nome: novoTreino.nome,
        objetivo: novoTreino.objetivo,
        observacoesGerais: novoTreino.observacoesGerais,
        }
      );
    setModalNovoTreino(false);
    navigate(`/treinos/${data.treino.id}/editar`);
  }


  function handleEditarTreino(aluno: Aluno) {
    const treino = treinos.find(
      (t) => t.id === aluno.id
    );

    if (!treino) return;

    navigate(`/treinos/${treino.id}/editar`);
  }

  if (loading) {
    return <p>Carregando...</p>;
  }

  return (
    <div className="dashboard-page">
      <HeaderInstrutor
        nome={user?.nome ?? ""}
        total={alunosVinculados.length}
        onLogout={logout}
      />

      <div className="dashboard-container">

         <AlunosVinculadosCard
          alunos={vinculadosFiltrados}
          busca={buscaVinculados}
          onBuscaChange={setBuscaVinculados}
          onEditar={handleEditarTreino}
          onRemover={handleDeletar} 
        />

        <AlunosDisponiveisCard
          alunos={disponiveisFiltrados}
          busca={buscaDisponiveis}
          onBuscaChange={setBuscaDisponiveis}
          onVincular={handleAbrirModalTreino}
        />
      </div>

      {modalNovoTreino && alunoParaVincular && (
        <div className="modal-overlay" onClick={() => setModalNovoTreino(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Novo Treino — {alunoParaVincular.nomeCompleto}</h3>

            <label>Nome do Treino *</label>
            <input
              type="text"
              value={novoTreino.nome}
              onChange={(e) => setNovoTreino((p) => ({ ...p, nome: e.target.value }))}
              placeholder="Ex: Treino de Hipertrofia"
              autoFocus
            />

            <label>Objetivo</label>
            <input
              type="text"
              value={novoTreino.objetivo}
              onChange={(e) => setNovoTreino((p) => ({ ...p, objetivo: e.target.value }))}
              placeholder="Ex: Hipertrofia, Emagrecimento, Força..."
            />

            <label>Observações Gerais</label>
            <input
              type="text"
              value={novoTreino.observacoesGerais}
              onChange={(e) => setNovoTreino((p) => ({ ...p, observacoesGerais: e.target.value }))}
              placeholder="Observações sobre o aluno ou treino..."
            />

            <div className="modal-acoes">
              <button className="btn-cancelar" onClick={() => setModalNovoTreino(false)}>
                Cancelar
              </button>
              <button
                className="btn-confirmar"
                onClick={handleConfirmarVincular}
                disabled={!novoTreino.nome.trim()}
              >
                Criar e Editar Treino
              </button>
            </div>
          </div>
        </div>
      )}
    </div>

    
  );
}