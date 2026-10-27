package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton

// Modelo de uma aula (dados de exemplo, sem banco por enquanto)
data class Aula(
    val nome: String,
    val horario: String,
    val instrutor: String,
    var vagas: Int,
    var inscrito: Boolean
) {
    // "08:00 - 09:00" -> "08:00"
    val horaInicio: String get() = horario.substringBefore(" - ")
}

class AulasActivity : AppCompatActivity() {

    private val idsDias = listOf(
        R.id.chipSeg, R.id.chipTer, R.id.chipQua, R.id.chipQui,
        R.id.chipSex, R.id.chipSab, R.id.chipDom
    )

    // Uma lista de aulas para cada dia, na ordem Seg, Ter, Qua, Qui, Sex, Sáb, Dom
    private val aulasPorDia = listOf(
        mutableListOf(
            Aula("Musculação Guiada", "07:00 - 08:00", "Carla Souza", 8, false),
            Aula("Spinning", "18:00 - 19:00", "Rafael Lima", 2, false)
        ),
        mutableListOf(
            Aula("Funcional", "07:00 - 08:00", "Bruna Alves", 6, false),
            Aula("Pilates", "09:00 - 10:00", "Camila Rocha", 0, false),
            Aula("Crossfit", "18:00 - 19:00", "Diego Martins", 3, false)
        ),
        mutableListOf(
            Aula("Zumba", "08:00 - 09:00", "Thiago Narak", 10, false),
            Aula("Boxe", "19:00 - 20:00", "Marcos Vieira", 5, true)
        ),
        mutableListOf(
            Aula("Ioga", "08:00 - 09:00", "Ronnison", 5, false),
            Aula("Zumba", "10:00 - 11:00", "Thiago Narak", 0, false),
            Aula("Jiu Jitsu", "19:00 - 20:00", "Nascimento", 4, true)
        ),
        mutableListOf(
            Aula("Spinning", "07:00 - 08:00", "Rafael Lima", 7, false),
            Aula("Ioga", "18:00 - 19:00", "Ronnison", 0, false)
        ),
        mutableListOf(
            Aula("Funcional", "09:00 - 10:00", "Bruna Alves", 12, false)
        ),
        mutableListOf<Aula>()
    )

    private var diaSelecionado = 3 // começa na quinta, como no protótipo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_aulas)
        ajustarBarras()

        // A seta leva de volta para a Home, limpando as telas que estiverem por cima dela
        findViewById<View>(R.id.btnVoltar).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        idsDias.forEachIndexed { indice, id ->
            findViewById<TextView>(id).setOnClickListener { selecionarDia(indice) }
        }

        BarraNavegacao.configurar(this, ItemNav.AULAS)
        selecionarDia(diaSelecionado)
    }

    private fun selecionarDia(indice: Int) {
        diaSelecionado = indice
        idsDias.forEachIndexed { i, id ->
            estilizarChip(findViewById(id), i == indice)
        }
        montarLista()
    }

    private fun estilizarChip(chip: TextView, ativo: Boolean) {
        chip.setBackgroundResource(if (ativo) R.drawable.bg_tab_ativa else R.drawable.bg_tab_inativa)
        chip.setTextColor(
            ContextCompat.getColor(this, if (ativo) android.R.color.white else R.color.fm_muted)
        )
    }

    // Monta um card para cada aula do dia selecionado
    private fun montarLista() {
        val container = findViewById<LinearLayout>(R.id.listaAulas)
        container.removeAllViews()

        val aulas = aulasPorDia[diaSelecionado]

        if (aulas.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Nenhuma aula neste dia."
            vazio.setTextColor(ContextCompat.getColor(this, R.color.fm_muted))
            vazio.textSize = 14f
            vazio.setPadding(0, (24 * resources.displayMetrics.density).toInt(), 0, 0)
            container.addView(vazio)
            return
        }

        for (aula in aulas) {
            val card = layoutInflater.inflate(R.layout.item_aula, container, false)

            card.findViewById<TextView>(R.id.txtNomeAula).text = aula.nome
            card.findViewById<TextView>(R.id.txtHorario).text = aula.horario
            card.findViewById<TextView>(R.id.txtInstrutor).text = aula.instrutor
            card.findViewById<TextView>(R.id.txtVagas).text = aula.vagas.toString()

            val botao = card.findViewById<MaterialButton>(R.id.btnAcaoAula)
            when {
                aula.inscrito -> {
                    botao.text = "Cancelar Inscrição"
                    Dialogos.aplicarEstilo(botao, EstiloBotao.PERIGO)
                    botao.setOnClickListener { confirmarCancelamento(aula) }
                }
                aula.vagas <= 0 -> {
                    botao.text = "Indisponível"
                    Dialogos.aplicarEstilo(botao, EstiloBotao.DESABILITADO)
                }
                else -> {
                    botao.text = "Inscrever"
                    Dialogos.aplicarEstilo(botao, EstiloBotao.PRIMARIO)
                    botao.setOnClickListener { confirmarInscricao(aula) }
                }
            }

            container.addView(card)
        }
    }

    private fun confirmarInscricao(aula: Aula) {
        Dialogos.confirmar(
            activity = this,
            titulo = "Confirmar Inscrição?",
            mensagem = "Você está prestes a se inscrever na aula de ${aula.nome}, às ${aula.horaInicio}.",
            textoEsquerda = "Cancelar",
            estiloEsquerda = EstiloBotao.CONTORNO,
            textoDireita = "Confirmar",
            estiloDireita = EstiloBotao.PRIMARIO
        ) {
            aula.inscrito = true
            aula.vagas -= 1
            montarLista()
            Toast.makeText(this, "Inscrição confirmada!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarCancelamento(aula: Aula) {
        Dialogos.confirmar(
            activity = this,
            titulo = "Cancelar Inscrição?",
            mensagem = "Você está prestes a cancelar sua vaga na aula de ${aula.nome}, às ${aula.horaInicio}.",
            textoEsquerda = "Sair",
            estiloEsquerda = EstiloBotao.PRIMARIO,
            textoDireita = "Cancelar Reserva",
            estiloDireita = EstiloBotao.PERIGO
        ) {
            aula.inscrito = false
            aula.vagas += 1
            montarLista()
            Toast.makeText(this, "Reserva cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerAulas)
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
