import type { Aluno } from "../../../services/alunoService";
import "../Dashboard.css";

interface Props {
  alunos: Aluno[];
  busca: string;
  onBuscaChange: (value: string) => void;
  onEditar: (aluno: Aluno) => void;
  onRemover: (aluno: Aluno) => void;
}

export function AlunosVinculadosCard({
  alunos,
  busca,
  onBuscaChange,
  onEditar,
  onRemover,
}: Props) {
  return (
    <div className="dashboard-card">
      <div className="card-header">
        <h2>Meus Alunos</h2>

        <span className="badge">
          {alunos.length} aluno(s) vinculado(s)
        </span>
      </div>

      <input
        type="text"
        placeholder="Buscar aluno por nome ou matrícula..."
        value={busca}
        onChange={(e) => onBuscaChange(e.target.value)}
        className="search-input"
      />

      {alunos.length === 0 ? (
        <p className="empty-state">
          {busca
            ? "Nenhum aluno encontrado."
            : "Nenhum aluno vinculado ainda."}
        </p>
      ) : (
        <table className="dashboard-table">
          <thead>
            <tr>
              <th>Nome</th>
              <th>Matrícula</th>
              <th>Ações</th>
            </tr>
          </thead>

          <tbody>
            {alunos.map((aluno) => (
              <tr key={aluno.id}>
                <td>{aluno.nomeCompleto}</td>

                <td>{aluno.matricula ?? "-"}</td>

                <td>
                  <div className="table-actions">
                    <button
                      className="btn-edit"
                      onClick={() => onEditar(aluno)}
                    >
                      ✏️ Editar Treino
                    </button>

                    <button
                      className="btn-delete"
                      onClick={() => onRemover(aluno)}
                    >
                      🗑️ Remover
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}