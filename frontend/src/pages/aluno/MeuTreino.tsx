import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './MeuTreino.css';
import { getCurrentUser, getToken } from "../../services/authService";

const API_BASE = 'https://fitmanagerapi-production.up.railway.app';

interface Instrutor {
  id: number;
  nomeCompleto: string;
}

interface TreinoInfo {
  treinoId: number;
  nomeTreino: string;
  status: string;
  instrutor: Instrutor;
  tempoEstimado: string;
  totalExercicios: number;
  observacoesGerais?: string;
}

interface Exercicio {
  idExercicio: number;
  nomeExercicio: string;
  series: number;
  repeticoes: number;
  carga: number;
  descanso: string;
  observacoes?: string;
  ordem: number;
}

interface Sessao {
  idExercicio: number;
  nomeSessao: string;
  grupoMuscular: string;
  ordem: number;
  exercicios: Exercicio[];
}

const LETRAS = ['A', 'B', 'C', 'D', 'E', 'F'];


function MeuTreino() {
     const navigate = useNavigate();
     const [treinoInfo, setTreinoInfo] = useState<TreinoInfo | null>(null);
     const [sessoes, setSessoes] = useState<Sessao[]>([]);
     const [loading, setLoading] = useState(true);
     const [erro, setErro] = useState('');

  useEffect(() => {
    const fetchTreino = async () => {
          try {
               const user = getCurrentUser();
               const token = getToken() ?? "";

               if (!user) {
               setErro("Sessão expirada. Faça login novamente.");
               setLoading(false);
               return;
               }

               const alunoId = user.id;
          
               const { data: infoData } = await axios.get<TreinoInfo>(
                    `${API_BASE}/api/v1/alunos/${alunoId}/treino`,
                    { headers: { Authorization: `Bearer ${token}` } }
               );

               setTreinoInfo(infoData);

               
               const { data: sessoesData } = await axios.get<{ treinoId: number; sessoes: Sessao[] }>(
                    `${API_BASE}/api/v1/treinos/${infoData.treinoId}/sessoes`,
                    { headers: { Authorization: `Bearer ${token}` } }
               );

               const sessoesOrdenadas = (sessoesData.sessoes ?? []).sort(
                    (a, b) => (a.ordem ?? 0) - (b.ordem ?? 0)
               );

               setSessoes(sessoesOrdenadas);

               } catch (err: unknown) {
               if (axios.isAxiosError(err)) {
                    const msg = err.response?.data?.message;
                    setErro(msg ?? `Erro ${err.response?.status ?? 'de conexão'}`);
               } else {
                    setErro('Erro inesperado ao carregar treino.');
               }
               } finally {
               setLoading(false);
               }
     };

     fetchTreino();
     }, []);

     
     if (loading) {
          return (
               <div className="mt-loading">
               <div className="mt-spinner" />
               <p>Carregando treino...</p>
               </div>
          );
     }

     if (erro && !treinoInfo) {
          return (
               <div className="mt-loading">
               <p className="mt-erro-msg">{erro}</p>
               <button className="mt-btn-retry" onClick={() => navigate('/aluno-dashboard')}>
                    ← Voltar ao Dashboard
               </button>
               </div>
          );
     }

     if (!treinoInfo) {
          return (
               <div className="mt-loading">
               <p>Nenhum treino encontrado.</p>
               <button className="mt-btn-retry" onClick={() => navigate('/aluno-dashboard')}>
                    ← Voltar ao Dashboard
               </button>
               </div>
          );
     }

  return (
    <div className="mt-wrapper">

     
      <header className="mt-cabecalho">
        <div className="mt-cabecalho-esquerda">
          <div className="mt-icone-header">📋</div>
          <div>
            <h1 className="mt-titulo">Minha Ficha de Treino</h1>
            <p className="mt-subtitulo">Visualização - Modo Aluno</p>
          </div>
        </div>
        <button className="mt-btn-voltar" onClick={() => navigate('/aluno-dashboard')}>
          ← Voltar
        </button>
      </header>

      <div className="mt-conteudo">

        
        <div className="mt-aviso">
          📋 Esta é sua ficha de treino. Você pode visualizar todos os detalhes, mas não
          pode editá-la. Para alterações, consulte seu professor.
        </div>

        
        <div className="mt-card">
          <h2 className="mt-card-titulo">Informações do Treino</h2>

          <div className="mt-info-grid">

            <div className="mt-info-item">
              <div className="mt-info-icone mt-info-icone--azul">👤</div>
              <div>
                <span className="mt-info-label">Professor Responsável</span>
                <span className="mt-info-valor">{treinoInfo.instrutor.nomeCompleto}</span>
              </div>
            </div>

            <div className="mt-info-item">
              <div className="mt-info-icone mt-info-icone--laranja">⏱</div>
              <div>
                <span className="mt-info-label">Tempo Estimado</span>
                <span className="mt-info-valor">{treinoInfo.tempoEstimado}</span>
              </div>
            </div>

            <div className="mt-info-item">
              <div className="mt-info-icone mt-info-icone--roxo">📄</div>
              <div>
                <span className="mt-info-label">Total de Exercícios</span>
                <span className="mt-info-valor">{treinoInfo.totalExercicios} exercícios</span>
              </div>
            </div>

            <div className="mt-info-item">
              <div className="mt-info-icone mt-info-icone--verde">🏷️</div>
              <div>
                <span className="mt-info-label">Nome do Treino</span>
                <span className="mt-info-valor">{treinoInfo.nomeTreino}</span>
              </div>
            </div>

          </div>

          <div className="mt-status">
            Status: ✓ Treino {treinoInfo.status}
          </div>

          {treinoInfo.observacoesGerais && (
            <div className="mt-obs-gerais">
              <strong>Orientações Gerais:</strong> {treinoInfo.observacoesGerais}
            </div>
          )}
        </div>

        
        {sessoes.length === 0 ? (
          <div className="mt-card">
            <p style={{ color: '#888', textAlign: 'center' }}>
              Nenhuma sessão cadastrada neste treino.
            </p>
          </div>
        ) : (
          sessoes.map((sessao, idx) => (
            <div key={`${sessao.nomeSessao}-${idx}`} className="mt-card mt-sessao">

              <div className="mt-sessao-header">
                <div className="mt-sessao-letra">{LETRAS[idx] ?? idx + 1}</div>
                <div>
                  <h3 className="mt-sessao-nome">{sessao.nomeSessao}</h3>
                  <p className="mt-sessao-qtd">
                    {sessao.grupoMuscular} · {sessao.exercicios?.length ?? 0} exercícios
                  </p>
                </div>
              </div>

              {(sessao.exercicios ?? []).length === 0 ? (
                <p style={{ color: '#aaa', fontSize: '0.85rem' }}>
                  Nenhum exercício cadastrado nesta sessão.
                </p>
              ) : (
                sessao.exercicios.map((ex) => (
                  <div key={ex.idExercicio} className="mt-exercicio">

                    <div className="mt-exercicio-topo">
                      <span className="mt-exercicio-nome">{ex.nomeExercicio}</span>
                      <span className="mt-exercicio-series">
                        {ex.series}x{ex.repeticoes}
                      </span>
                    </div>

                    <div className="mt-exercicio-detalhes">
                      <div className="mt-detalhe">
                        <span className="mt-detalhe-label">Carga</span>
                        <span className="mt-detalhe-valor">{ex.carga} kg</span>
                      </div>
                      <div className="mt-detalhe">
                        <span className="mt-detalhe-label">Descanso</span>
                        <span className="mt-detalhe-valor">{ex.descanso}</span>
                      </div>
                      <div className="mt-detalhe">
                        <span className="mt-detalhe-label">Séries x Reps</span>
                        <span className="mt-detalhe-valor">
                          {ex.series} x {ex.repeticoes}
                        </span>
                      </div>
                    </div>

                    {ex.observacoes && (
                      <div className="mt-observacao">
                        <span className="mt-observacao-label">Observação do Professor: </span>
                        {ex.observacoes}
                      </div>
                    )}

                  </div>
                ))
              )}

            </div>
          ))
        )}

      </div>
    </div>
  );
}

export default MeuTreino;
