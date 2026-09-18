import type { RelatorioTreino } from "../../../../services/treinoService";

interface Props {
  relatorio: RelatorioTreino;
  onVoltar: () => void;
  onVoltarDashboard: () => void;
}

export function RelatorioView({ relatorio, onVoltar, onVoltarDashboard }: Props) {
  return (
    <div className="relatorio-page">
      <header className="treino-header">
        <div className="treino-header-left">
          <div className="treino-avatar">📋</div>
          <div>
            <div className="treino-title">Relatório de Treino Gerado</div>
            <div className="treino-subtitle">Treino salvo com sucesso</div>
          </div>
        </div>
      </header>

      <div className="relatorio-container">
        <div className="relatorio-card">
          <h2>Resumo do Treino</h2>
          <div className="resumo-grid">
            <div className="resumo-item">
              <span className="resumo-label">Total de Exercícios</span>
              <strong className="resumo-valor">{relatorio.quantidadeExercicios}</strong>
            </div>
            <div className="resumo-item resumo-green">
              <span className="resumo-label">Tempo Estimado</span>
              <strong className="resumo-valor">{relatorio.tempoEstimado}</strong>
            </div>
            <div className="resumo-item resumo-orange">
              <span className="resumo-label">Professor</span>
              <strong className="resumo-valor">{relatorio.instrutor}</strong>
            </div>
            <div className="resumo-item resumo-purple">
              <span className="resumo-label">Status</span>
              <strong className="resumo-valor resumo-status">✓ {relatorio.status}</strong>
            </div>
          </div>
        </div>

        {(relatorio.sessoes ?? []).map((sessao, idx) => (
          <div key={idx} className="relatorio-card">
            <h3 className="sessao-titulo">{sessao.nome}</h3>
            <div className="exercicios-relatorio">
              {sessao.exercicios.map((ex, exIdx) => (
                <div key={exIdx} className="exercicio-relatorio-item">
                  <div className="exercicio-relatorio-nome">{ex.nome}</div>
                  <div className="exercicio-relatorio-info">
                    {ex.series}x{ex.repeticoes} | Carga: {ex.carga}kg | Descanso: {ex.descanso}s
                  </div>
                  {ex.observacoes && (
                    <div className="exercicio-relatorio-obs">{ex.observacoes}</div>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}

        <div className="relatorio-acoes">
          <button className="btn-voltar-edicao" onClick={onVoltar}>
            Voltar para Edição
          </button>
          <button className="btn-voltar-dashboard" onClick={onVoltarDashboard}>
            Voltar para Dashboard
          </button>
        </div>
      </div>
    </div>
  );
}