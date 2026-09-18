import type { Sessao } from "../../../../services/treinoService";

interface Props {
  sessoes: Sessao[];
  ativa: number;
  onSelecionar: (idx: number) => void;
  onAdicionar: () => void;
}

export function SessoesNav({ sessoes, ativa, onSelecionar, onAdicionar }: Props) {
  return (
    <div className="sessoes-nav">
      <div className="sessoes-tabs">
        {sessoes.map((sessao, idx) => (
          <button
            key={idx}
            className={`sessao-tab ${ativa === idx ? "ativa" : ""}`}
            onClick={() => onSelecionar(idx)}
          >
            {sessao.nome || `Treino ${String.fromCharCode(65 + idx)}`}
          </button>
        ))}
        <button className="sessao-tab sessao-tab-add" onClick={onAdicionar}>
          + Nova Divisão
        </button>
      </div>
    </div>
  );
}