import type { Aluno } from "../../../services/alunoService";
import "../Dashboard.css";

interface Props {
  alunos: Aluno[];
  busca: string;
  onBuscaChange: (value: string) => void;
  onVincular: (aluno: Aluno) => void;
}

export function AlunosDisponiveisCard({
  alunos,
  busca,
  onBuscaChange,
  onVincular,
}: Props) {
  return (
    <div className="dashboard-card">
      <div className="card-header">
        <h2>Vincular Novo Aluno</h2>

        <span className="badge">
          {alunos.length} aluno(s) disponível(is)
        </span>
      </div>

      <input
        type="text"
        placeholder="Buscar aluno para vincular..."
        value={busca}
        onChange={(e) => onBuscaChange(e.target.value)}
        className="search-input"
      />

      {alunos.length === 0 ? (
        <p className="empty-state">
          {busca
            ? "Nenhum aluno encontrado."
            : "Todos os alunos já estão vinculados."}
        </p>
      ) : (
        <table className="dashboard-table">
          <thead>
            <tr>
              <th>Nome</th>
              <th>Matrícula</th>
              <th>Ação</th>
            </tr>
          </thead>

          <tbody>
            {alunos.map((aluno) => (
              <tr key={aluno.id}>
                <td>{aluno.nomeCompleto}</td>

                <td>{aluno.matricula ?? "-"}</td>

                <td>
                  <button
                    className="btn-add"
                    onClick={() => onVincular(aluno)}
                  >
                    + Adicionar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}