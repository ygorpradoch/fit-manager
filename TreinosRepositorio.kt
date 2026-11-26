package com.example.fitmanager

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// Leitura dos treinos do aluno no Firestore: usuarios/{uid}/treinos/{id}
//
// Campos de cada treino:
//   nome        (String)  título do card, ex.: "Peito"
//   tipo        (String)  "Musculação", "Funcional" ou "Cardio"
//   dias        (Array)   códigos dos dias: "seg", "ter", "qua", "qui", "sex", "sab", "dom"
//   ordem       (Number)  posição do treino na lista (opcional, padrão 0)
//   exercicios  (Array de Map) cada item com: nome, series, repeticoes, carga, descanso, equipamentos
object TreinosRepositorio {

    // APENAS PARA DEMONSTRAÇÃO: enquanto o Montar Treino do admin não grava no banco, quem não tem
    // nenhum treino recebe os treinos de exemplo abaixo. Troque para false quando ele estiver gravando.
    private const val CRIAR_EXEMPLOS = true

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val criandoExemplos = mutableSetOf<String>()

    private fun colecao(uid: String) =
        db.collection("usuarios").document(uid).collection("treinos")

    // Lê todos os treinos do aluno e avisa sempre que algo mudar no banco
    fun observar(
        uid: String,
        aoMudar: (List<Treino>) -> Unit,
        aoErro: (Exception) -> Unit
    ): ListenerRegistration {
        return colecao(uid).addSnapshotListener { resultado, erro ->
            if (erro != null) {
                aoErro(erro)
                return@addSnapshotListener
            }
            if (resultado == null) return@addSnapshotListener

            // Banco confirmou que está vazio: cria os treinos de exemplo (a tela atualiza sozinha depois)
            if (CRIAR_EXEMPLOS && resultado.isEmpty && !resultado.metadata.isFromCache) {
                criarExemplos(uid)
                return@addSnapshotListener
            }

            val treinos = resultado.documents
                .mapNotNull { converter(it) }
                .sortedWith(compareBy({ it.ordem }, { it.nome }))
            aoMudar(treinos)
        }
    }

    // Lê um único treino (usado na tela de detalhe do exercício)
    fun observarTreino(
        uid: String,
        treinoId: String,
        aoMudar: (Treino?) -> Unit,
        aoErro: (Exception) -> Unit
    ): ListenerRegistration {
        return colecao(uid).document(treinoId).addSnapshotListener { doc, erro ->
            if (erro != null) {
                aoErro(erro)
                return@addSnapshotListener
            }
            if (doc == null) return@addSnapshotListener

            // Se ainda não está no cache, espera a resposta do servidor antes de dizer que não existe
            if (!doc.exists() && doc.metadata.isFromCache) return@addSnapshotListener

            aoMudar(if (doc.exists()) converter(doc) else null)
        }
    }

    private fun converter(doc: DocumentSnapshot): Treino? {
        val nome = doc.getString("nome") ?: return null

        val exercicios = (doc.get("exercicios") as? List<*>).orEmpty().mapNotNull { item ->
            val mapa = item as? Map<*, *> ?: return@mapNotNull null
            ExercicioTreino(
                nome = mapa["nome"] as? String ?: return@mapNotNull null,
                series = (mapa["series"] as? Number)?.toInt() ?: 0,
                repeticoes = (mapa["repeticoes"] as? Number)?.toInt() ?: 0,
                carga = mapa["carga"] as? String ?: "",
                descanso = mapa["descanso"] as? String ?: "",
                equipamentos = (mapa["equipamentos"] as? List<*>).orEmpty().filterIsInstance<String>()
            )
        }

        return Treino(
            id = doc.id,
            nome = nome,
            tipo = doc.getString("tipo").orEmpty(),
            dias = (doc.get("dias") as? List<*>).orEmpty().filterIsInstance<String>(),
            ordem = doc.getLong("ordem")?.toInt() ?: 0,
            exercicios = exercicios
        )
    }

    // ---------------------------------------------------------------------------------------
    // APENAS PARA DEMONSTRAÇÃO (ver CRIAR_EXEMPLOS). Os ids são fixos, então criar duas vezes
    // não duplica nada.
    // ---------------------------------------------------------------------------------------

    private fun criarExemplos(uid: String) {
        if (!criandoExemplos.add(uid)) return

        val lote = db.batch()
        EXEMPLOS.forEach { (id, dados) -> lote.set(colecao(uid).document(id), dados) }
        lote.commit().addOnFailureListener { e ->
            criandoExemplos.remove(uid)
            Log.e("FIREBASE", "Erro ao criar os treinos de exemplo", e)
        }
    }

    private fun exercicio(
        nome: String,
        series: Int,
        repeticoes: Int,
        carga: String,
        descanso: String,
        vararg equipamentos: String
    ): Map<String, Any> = mapOf(
        "nome" to nome,
        "series" to series,
        "repeticoes" to repeticoes,
        "carga" to carga,
        "descanso" to descanso,
        "equipamentos" to equipamentos.toList()
    )

    private val EXEMPLOS: Map<String, Map<String, Any>> = mapOf(
        "exemplo_peito" to mapOf(
            "nome" to "Peito",
            "tipo" to "Musculação",
            "dias" to listOf("seg", "qui"),
            "ordem" to 1,
            "exercicios" to listOf(
                exercicio("Supino Reto", 4, 12, "20kg", "60s", "Banco de Supino", "Barra", "2x Anilhas de 10kg"),
                exercicio("Supino Inclinado", 4, 12, "25kg", "60s", "Banco Inclinado", "Barra", "2x Anilhas de 10kg"),
                exercicio("Voador", 3, 12, "45kg", "60s", "Máquina Voador")
            )
        ),
        "exemplo_costas" to mapOf(
            "nome" to "Costas",
            "tipo" to "Musculação",
            "dias" to listOf("ter", "sex"),
            "ordem" to 2,
            "exercicios" to listOf(
                exercicio("Puxada Frontal", 4, 15, "40kg", "60s", "Polia Alta", "Barra de Puxada"),
                exercicio("Remada Alta", 3, 15, "45kg", "60s", "Máquina de Remada"),
                exercicio("Remada Baixa", 3, 15, "30kg", "60s", "Polia Baixa", "Triângulo")
            )
        ),
        "exemplo_perna" to mapOf(
            "nome" to "Perna",
            "tipo" to "Musculação",
            "dias" to listOf("qua", "sab", "dom"),
            "ordem" to 3,
            "exercicios" to listOf(
                exercicio("Agachamento", 4, 12, "40kg", "90s", "Barra", "Rack de Agachamento", "Anilhas"),
                exercicio("Leg Press", 3, 12, "80kg", "90s", "Máquina Leg Press"),
                exercicio("Cadeira Extensora", 3, 12, "45kg", "60s", "Cadeira Extensora"),
                exercicio("Stiff", 3, 12, "30kg", "60s", "Barra", "Anilhas"),
                exercicio("Mesa Flexora", 3, 12, "50kg", "60s", "Mesa Flexora")
            )
        )
    )
}
