import { useState, useEffect, useCallback } from "react";
import {
  getSessoesDeTreino,
  criarSessao,
  deletarSessao,
  criarExercicio,
  atualizarExercicio,
  deletarExercicio,
  getRelatorio,
  type Sessao,
  type Exercicio,
  type RelatorioTreino,
} from "../services/treinoService";

export function useTreino(treinoId: number) {
    const [sessoes, setSessoes] = useState<Sessao[]>([]);
    const [sessaoAtiva, setSessaoAtiva] = useState<number>(0); // índice
    const [loading, setLoading] = useState(true);
    const [salvando, setSalvando] = useState(false);
    const [erro, setErro] = useState("");
    const [relatorio, setRelatorio] = useState<RelatorioTreino | null>(null);
    const [viewRelatorio, setViewRelatorio] = useState(false);

    const carregarSessoes = useCallback(async () => {
        setLoading(true);
        setErro("");
        try {
           const data = await getSessoesDeTreino(treinoId);
            setSessoes(data);     
        } catch (e) {
        
            setErro(e instanceof Error ? e.message : "Erro ao carregar sessões.");
        } finally {
            setLoading(false);
        }
    }, [treinoId]);

    useEffect(() => {
        carregarSessoes();
    }, [carregarSessoes]);


    async function adicionarSessao(nome: string, grupoMuscular: string) {
        const nova = await criarSessao(treinoId, nome, grupoMuscular);
        const novaComExercicios = { ...nova, exercicios: nova.exercicios ?? [] };
        setSessoes((prev) => [...prev, novaComExercicios]);
        setSessaoAtiva(sessoes.length);
    }

    async function removerSessao(sessaoId: number) {
        await deletarSessao(sessaoId);
        setSessoes((prev) => prev.filter((s) => s.id !== sessaoId));
        setSessaoAtiva(0);
    }

    function adicionarExercicioLocal(sessaoIdx: number) {
        const vazio: Exercicio = {
            nome: "",
            series: 3,
            repeticoes: 12,
            descanso: 60,   
            carga: 0,       
            observacoes: "",
        };
        setSessoes((prev) =>
        prev.map((s, i) =>
            i === sessaoIdx
            ? { ...s, exercicios: [...s.exercicios, vazio] }
            : s
        )
        );
    }

    function atualizarExercicioLocal(
        sessaoIdx: number,
        exIdx: number,
        campo: keyof Exercicio,
        valor: string | number
    ) {
        setSessoes((prev) =>
        prev.map((s, i) =>
            i === sessaoIdx
            ? {
                ...s,
                exercicios: s.exercicios.map((e, j) =>
                    j === exIdx ? { ...e, [campo]: valor } : e
                ),
                }
            : s
        )
        );
    }

    async function removerExercicio(sessaoIdx: number, exIdx: number) {
        const ex = sessoes[sessaoIdx].exercicios[exIdx];
        if (ex.id) await deletarExercicio(ex.id);
        setSessoes((prev) =>
        prev.map((s, i) =>
            i === sessaoIdx
            ? { ...s, exercicios: s.exercicios.filter((_, j) => j !== exIdx) }
            : s
        )
        );
    }

    async function salvarTudo() {
        setSalvando(true);
        setErro("");
        try {
        for (const sessao of sessoes) {
            if (!sessao.id) continue;
            for (const ex of sessao.exercicios) {
            const payload = {
                nome: ex.nome,
                series: ex.series,
                repeticoes: ex.repeticoes,
                descanso: ex.descanso,
                carga: ex.carga,
                observacoes: ex.observacoes,
            };
            if (ex.id) {
                await atualizarExercicio(ex.id, payload);
            } else {
                const criado = await criarExercicio(sessao.id, payload);
                ex.id = criado.id; // atualiza o id local
            }
            }
        }
        } catch (e) {
        setErro(e instanceof Error ? e.message : "Erro ao salvar treino.");
        setSalvando(false);
        throw e;
        }
        setSalvando(false);
    }

    async function finalizarEGerarRelatorio() {
        await salvarTudo();
        const rel = await getRelatorio(treinoId);
        setRelatorio({ ...rel, sessoes });
        setViewRelatorio(true);
    }

    return {
        sessoes,
        sessaoAtiva,
        setSessaoAtiva,
        loading,
        salvando,
        erro,
        relatorio,
        viewRelatorio,
        setViewRelatorio,
        adicionarSessao,
        removerSessao,
        adicionarExercicioLocal,
        atualizarExercicioLocal,
        removerExercicio,
        finalizarEGerarRelatorio,
    };
}