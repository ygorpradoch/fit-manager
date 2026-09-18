import type { Sessao, Exercicio } from "../../../../services/treinoService";
import { ExercicioForm } from "./ExercicioForm";

interface Props {
  sessao: Sessao;
  onAdicionarExercicio: () => void;
  onAlterarExercicio: (exIdx: number, campo: keyof Exercicio, valor: string | number) => void;
  onRemoverExercicio: (exIdx: number) => void;
}

export function SessaoCard({
  sessao,
  onAdicionarExercicio,
  onAlterarExercicio,
  onRemoverExercicio,
}: Props) {
  return (
    <div className="sessao-card">
      <div className="sessao-card-header">
        <h2>Exercícios</h2>
        <button className="btn-add-exercicio" onClick={onAdicionarExercicio}>
          + Adicionar Exercício
        </button>
      </div>

      {sessao.exercicios.length === 0 ? (
        <p className="empty-exercicios">
          Nenhum exercício ainda. Clique em "+ Adicionar Exercício" para começar.
        </p>
      ) : (
        <div className="exercicios-lista">
          {sessao.exercicios.map((ex, idx) => (
            <ExercicioForm
              key={idx}
              exercicio={ex}
              onChange={(campo, valor) => onAlterarExercicio(idx, campo, valor)}
              onRemover={() => onRemoverExercicio(idx)}
            />
          ))}
        </div>
      )}
    </div>
  );
}