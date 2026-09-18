import "../Dashboard.css";

interface Props {
  nome: string;
  total: number;
  onLogout: () => void;
}

export function HeaderInstrutor({
  nome,
  total,
  onLogout,
}: Props) {
  return (
    <header className="instrutor-header">

      <div className="instrutor-header-left">
        <div className="instrutor-avatar">
          🎓
        </div>

        <div>
          <div className="instrutor-title">
            Área do Professor
          </div>

          <div className="instrutor-subtitle">
            {nome}
          </div>
        </div>
      </div>

      <div className="instrutor-header-right">
        <div className="instrutor-total-card">
          <div>Total de Alunos</div>
          <strong>{total}</strong>
        </div>

        <button
          className="btn-logout"
          onClick={onLogout}
        >
          Sair
        </button>
      </div>

    </header>
  );
}