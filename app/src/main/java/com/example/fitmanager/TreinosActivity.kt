package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

// Modelo de um exercício (dados de exemplo, sem banco por enquanto)
data class Exercicio(
    val nome: String,
    val grupo: String,
    val carga: String,
    val series: Int,
    val repeticoes: Int,
    val equipamentos: List<String>
)

class TreinosActivity : AppCompatActivity() {

    private val exercicios = listOf(
        Exercicio("Supino Reto", "Peito", "20kg", 4, 12,
            listOf("Banco de Supino", "Barra", "2x Anilhas de 10kg")),
        Exercicio("Supino Inclinado", "Peito", "25kg", 4, 12,
            listOf("Banco Inclinado", "Barra", "2x Anilhas de 10kg")),
        Exercicio("Voador", "Peito", "45kg", 3, 12,
            listOf("Máquina Voador")),

        Exercicio("Puxada Frontal", "Costas", "40kg", 4, 15,
            listOf("Polia Alta", "Barra de Puxada")),
        Exercicio("Remada Alta", "Costas", "45kg", 3, 15,
            listOf("Máquina de Remada")),
        Exercicio("Remada Baixa", "Costas", "30kg", 3, 15,
            listOf("Polia Baixa", "Triângulo")),

        Exercicio("Agachamento", "Perna", "40kg", 4, 12,
            listOf("Barra", "Rack de Agachamento", "Anilhas")),
        Exercicio("Leg Press", "Perna", "80kg", 3, 12,
            listOf("Máquina Leg Press")),
        Exercicio("Cadeira Extensora", "Perna", "45kg", 3, 12,
            listOf("Cadeira Extensora"))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_treinos)
        ajustarBarras()

        // A seta leva de volta para a Home, limpando as telas que estiverem por cima dela
        findViewById<View>(R.id.btnVoltar).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        montarLista()
        BarraNavegacao.configurar(this, ItemNav.TREINOS)
    }

    // Monta um card por grupo muscular, com uma linha para cada exercício
    private fun montarLista() {
        val container = findViewById<LinearLayout>(R.id.listaTreinos)
        container.removeAllViews()

        for ((grupo, lista) in exercicios.groupBy { it.grupo }) {
            val card = layoutInflater.inflate(R.layout.item_grupo_treino, container, false)
            card.findViewById<TextView>(R.id.txtGrupo).text = grupo.uppercase()
            val linhas = card.findViewById<LinearLayout>(R.id.containerExercicios)

            lista.forEachIndexed { indice, exercicio ->
                val linha = layoutInflater.inflate(R.layout.item_exercicio_linha, linhas, false)
                linha.findViewById<TextView>(R.id.txtNomeExercicio).text = exercicio.nome
                linha.findViewById<TextView>(R.id.txtCarga).text =
                    "${exercicio.carga} · ${exercicio.repeticoes}x"

                // O último exercício do card fica sem divisor
                linha.findViewById<View>(R.id.divisor).visibility =
                    if (indice == lista.lastIndex) View.GONE else View.VISIBLE

                linha.findViewById<View>(R.id.linhaExercicio).setOnClickListener {
                    abrirDetalhe(exercicio)
                }
                linhas.addView(linha)
            }
            container.addView(card)
        }
    }

    // Envia os dados do exercício escolhido para a tela de detalhe
    private fun abrirDetalhe(exercicio: Exercicio) {
        val intent = Intent(this, DetalheExercicioActivity::class.java)
        intent.putExtra(DetalheExercicioActivity.EXTRA_NOME, exercicio.nome)
        intent.putExtra(DetalheExercicioActivity.EXTRA_GRUPO, exercicio.grupo)
        intent.putExtra(DetalheExercicioActivity.EXTRA_SERIES, exercicio.series)
        intent.putExtra(DetalheExercicioActivity.EXTRA_REPETICOES, exercicio.repeticoes)
        intent.putStringArrayListExtra(
            DetalheExercicioActivity.EXTRA_EQUIPAMENTOS,
            ArrayList(exercicio.equipamentos)
        )
        startActivity(intent)
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerTreinos)
        val bottomNav = findViewById<View>(R.id.bottomNav)
        val topOriginal = header.paddingTop
        val bottomOriginal = bottomNav.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, 0)
            header.updatePadding(top = topOriginal + bars.top)
            bottomNav.updatePadding(bottom = bottomOriginal + bars.bottom)
            insets
        }
    }
}