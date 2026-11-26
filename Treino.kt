package com.example.fitmanager

import java.time.DayOfWeek
import java.time.LocalDate

// Um exercício dentro de um treino (fica no array "exercicios" do documento do treino)
data class ExercicioTreino(
    val nome: String,
    val series: Int,
    val repeticoes: Int,
    val carga: String,
    val descanso: String,
    val equipamentos: List<String>
) {
    // "40kg · 12x" (linha do exercício na Home e na tela de Treinos)
    val resumoCarga: String
        get() = if (carga.isBlank()) "${repeticoes}x" else "$carga · ${repeticoes}x"
}

// Um treino do aluno, guardado em usuarios/{uid}/treinos/{id}
// O "nome" é o título do card (ex.: "Peito") e "dias" diz em quais dias da semana ele acontece
data class Treino(
    val id: String,
    val nome: String,
    val tipo: String,
    val dias: List<String>,
    val ordem: Int,
    val exercicios: List<ExercicioTreino>
)

// Regras de "qual treino é de hoje" e "quais são os próximos"
object DiasTreino {

    // Códigos usados no campo "dias" do Firestore
    val TODOS = listOf("seg", "ter", "qua", "qui", "sex", "sab", "dom")

    private val NOMES = mapOf(
        "segunda" to "seg",
        "terça" to "ter",
        "terca" to "ter",
        "quarta" to "qua",
        "quinta" to "qui",
        "sexta" to "sex",
        "sábado" to "sab",
        "sabado" to "sab",
        "domingo" to "dom"
    )

    fun codigoDoDia(data: LocalDate): String = when (data.dayOfWeek) {
        DayOfWeek.MONDAY -> "seg"
        DayOfWeek.TUESDAY -> "ter"
        DayOfWeek.WEDNESDAY -> "qua"
        DayOfWeek.THURSDAY -> "qui"
        DayOfWeek.FRIDAY -> "sex"
        DayOfWeek.SATURDAY -> "sab"
        DayOfWeek.SUNDAY -> "dom"
    }

    // O treino de hoje é o primeiro da lista (já ordenada) que acontece no dia da semana de hoje
    fun treinoDoDia(treinos: List<Treino>, hoje: LocalDate): Treino? {
        val codigo = codigoDoDia(hoje)
        return treinos.firstOrNull { codigo in it.dias }
    }

    // Os próximos treinos: olha os dias seguintes e pega os primeiros que não sejam o de hoje
    fun proximos(treinos: List<Treino>, hoje: LocalDate, quantidade: Int = 2): List<Treino> {
        val doDia = treinoDoDia(treinos, hoje)
        val resultado = mutableListOf<Treino>()

        for (distancia in 1L..7L) {
            val codigo = codigoDoDia(hoje.plusDays(distancia))
            for (treino in treinos) {
                if (codigo !in treino.dias) continue
                if (treino.id == doDia?.id || resultado.any { it.id == treino.id }) continue
                resultado.add(treino)
                if (resultado.size == quantidade) return resultado
            }
        }
        return resultado
    }

    // Converte o texto do menu "Dias da semana" do Montar Treino nos códigos guardados no Firestore
    // Ex.: "Segunda e Quinta" -> ["seg", "qui"]; "Todos os dias" -> todos
    fun paraCodigos(texto: String): List<String> {
        val minusculo = texto.lowercase(LOCALE_BR)
        if (minusculo.contains("todos")) return TODOS
        return NOMES.filterKeys { minusculo.contains(it) }
            .values
            .distinct()
            .sortedBy { TODOS.indexOf(it) }
    }
}
