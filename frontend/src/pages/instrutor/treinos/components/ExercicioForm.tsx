import type { Exercicio } from "../../../../services/treinoService";

interface Props {
  exercicio: Exercicio;
  onChange: (campo: keyof Exercicio, valor: string | number) => void;
  onRemover: () => void;
}

export function ExercicioForm({ exercicio, onChange, onRemover }: Props) {
  return (
    <div className="exercicio-card">
      <div className="exercicio-row">
        <div className="campo campo-nome">
          <label>Nome do Exercício</label>
          <input
            type="text"
            value={exercicio.nome}
            placeholder="Ex: Supino Reto"
            onChange={(e) => onChange("nome", e.target.value)}
          />
        </div>

        <div className="campo campo-sm">
          <label>Séries</label>
          <input
            type="number"
            value={exercicio.series}
            min={1}
            onChange={(e) => onChange("series", Number(e.target.value))}
          />
        </div>

        <div className="campo campo-sm">
          <label>Repetições</label>
          <input
            type="number"
            value={exercicio.repeticoes}
            min={1}
            onChange={(e) => onChange("repeticoes", Number(e.target.value))}
          />

          <div className="campo campo-sm">
            <label>Descanso (seg)</label>
            <input
              type="number"
              value={exercicio.descanso}
              placeholder="60"
              onChange={(e) => onChange("descanso", Number(e.target.value))}
            />
          </div>

          <div className="campo campo-sm">
            <label>Carga (kg)</label>
            <input
              type="number"
              value={exercicio.carga}
              placeholder="60"
              onChange={(e) => onChange("carga", Number(e.target.value))}
            />
          </div>
        </div>
      </div>
      <div className="exercicio-row exercicio-row-obs">
        <div className="campo campo-obs">
          <label>Observações</label>
          <input
            type="text"
            value={exercicio.observacoes}
            placeholder="Atenção à postura, manter execução controlada..."
            onChange={(e) => onChange("observacoes", e.target.value)}
          />
        </div>
        <button className="btn-remover" onClick={onRemover}>
          🗑️ Remover
        </button>
      </div>
    </div>
  );
}