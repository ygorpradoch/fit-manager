import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTreino } from "../../../hooks/useTreino";
import { SessoesNav } from "./components/SessoesNav";
import { SessaoCard } from "./components/SessaoCard";
import { RelatorioView } from "./components/RelatorioView";
import "./GerenciarTreino.css";

export function GerenciarTreino() {
    const { treinoId } = useParams<{ treinoId: string }>();
    const navigate = useNavigate();
    const id = Number(treinoId);

    const [modalNovaSessao, setModalNovaSessao] = useState(false);
    const [grupoMuscular, setGrupoMuscular] = useState("");

    const {
        sessoes,
        sessaoAtiva,
        setSessaoAtiva,
        loading,
        salvando,
        relatorio,
        viewRelatorio,
        setViewRelatorio,
        adicionarSessao,
        adicionarExercicioLocal,
        atualizarExercicioLocal,
        removerExercicio,
        finalizarEGerarRelatorio,
    } = useTreino(id);

    function nomeCompletoSessao(): string {
        const letra = String.fromCharCode(65 + sessoes.length);
        const grupo = grupoMuscular.trim() || "Treino";
        return `Treino ${letra} - ${grupo}`;
    }

    async function handleAdicionarSessao() {
        if (!grupoMuscular.trim()) return;
        await adicionarSessao(nomeCompletoSessao(), grupoMuscular.trim());
        setGrupoMuscular("");
        setModalNovaSessao(false);
    }

    async function handleFinalizar() {
        try {
        await finalizarEGerarRelatorio();
        } catch {
        // erro ta setadi no hook 
        }
    }

    if (loading) return <div className="treino-loading">Carregando treino...</div>;

    if (viewRelatorio && relatorio) {
        return (
        <RelatorioView
            relatorio={relatorio}
            onVoltar={() => setViewRelatorio(false)}
            onVoltarDashboard={() => navigate("/instrutor-dashboard")}
        />
        );
    }

    const sessaoAtual = sessoes[sessaoAtiva];

    return (
        <div className="gerenciar-treino-page">
        <header className="treino-header">
            <div className="treino-header-left">
            <div>
                <div className="treino-title">Gerenciar Treinos</div>
                <div className="treino-subtitle">Edição de fichas de treino - Professor</div>
            </div>
            </div>
        </header>

        <div className="treino-content">
            <SessoesNav
            sessoes={sessoes}
            ativa={sessaoAtiva}
            onSelecionar={setSessaoAtiva}
            onAdicionar={() => setModalNovaSessao(true)}
            />

            {sessaoAtual ? (
            <SessaoCard
                sessao={sessaoAtual}
                onAdicionarExercicio={() => adicionarExercicioLocal(sessaoAtiva)}
                onAlterarExercicio={(exIdx, campo, valor) =>
                atualizarExercicioLocal(sessaoAtiva, exIdx, campo, valor)
                }
                onRemoverExercicio={(exIdx) => removerExercicio(sessaoAtiva, exIdx)}
            />
            ) : (
            <div className="treino-empty">
                Nenhuma divisão criada. Clique em "+ Nova Divisão" para começar.
            </div>
            )}
        </div>

        <div className="treino-footer">
            <button
            className="btn-finalizar"
            onClick={handleFinalizar}
            disabled={salvando}
            >
            {salvando ? "Salvando..." : "Finalizar e Gerar Relatório"}
            </button>
        </div>

        {modalNovaSessao && (
            <div className="modal-overlay" onClick={() => setModalNovaSessao(false)}>
                <div className="modal" onClick={(e) => e.stopPropagation()}>
                <h3>Nova Divisão de Treino</h3>
                <p>
                    Será criada como:{" "}
                    <strong>
                    Treino {String.fromCharCode(65 + sessoes.length)} -{" "}
                    {grupoMuscular.trim() || "..."}
                    </strong>
                </p>
                <input
                    type="text"
                    value={grupoMuscular}
                    onChange={(e) => setGrupoMuscular(e.target.value)}
                    placeholder="Ex: Peito/Tríceps, Costas/Bíceps, Pernas..."
                    autoFocus
                    onKeyDown={(e) => e.key === "Enter" && handleAdicionarSessao()}
                />
                <div className="modal-acoes">
                    <button className="btn-cancelar" onClick={() => setModalNovaSessao(false)}>
                    Cancelar
                    </button>
                    <button
                    className="btn-confirmar"
                    onClick={handleAdicionarSessao}
                    disabled={!grupoMuscular.trim()}
                    >
                    Criar
                    </button>
                </div>
                </div>
            </div>
            )}
        </div>
    );
}