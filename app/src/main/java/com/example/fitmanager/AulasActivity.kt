package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class AulasActivity : AppCompatActivity() {

    // Aula de exemplo (sem banco por enquanto)
    private class Aula(
        val nome: String,
        val inicio: String,
        val fim: String,
        val instrutor: String,
        var vagas: Int,
        var inscrito: Boolean = false
    )

    private val dias = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

    // Índice do dia (0 = Seg ... 6 = Dom) -> aulas daquele dia
    private val aulasPorDia: Map<Int, List<Aula>> = mapOf(
        0 to listOf(
            Aula("Yoga", "08:00", "09:00", "Ronnison", 12),
            Aula("Spinning", "18:00", "19:00", "Nascimento", 3)
        ),
        1 to listOf(
            Aula("Boxe", "07:00", "08:00", "Nascimento", 6),
            Aula("Zumba", "19:00", "20:00", "Thiago Narak", 8)
        ),
        2 to listOf(
            Aula("Ioga", "08:00", "09:00", "Ronnison", 4),
            Aula("Crossfit", "18:00", "19:00", "Thiago Narak", 0)
        ),
        3 to listOf(
            Aula("Ioga", "08:00", "09:00", "Ronnison", 5),
            Aula("Zumba", "10:00", "11:00", "Thiago Narak", 0),
            Aula("Jiu Jitsu", "19:00", "20:00", "Nascimento", 4, inscrito = true)
        ),
        4 to listOf(
            Aula("Spinning", "07:00", "08:00", "Nascimento", 10),
            Aula("Boxe", "18:00", "19:00", "Nascimento", 2)
        ),
        5 to listOf(
            Aula("Zumba", "09:00", "10:00", "Thiago Narak", 15)
        ),
        6 to emptyList()
    )

    private var diaSelecionado = 0
    private val abasDias = mutableListOf<TextView>()

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

        diaSelecionado = diaDeHoje()
        montarDias()
        montarLista()
        BarraNavegacao.configurar(this, ItemNav.AULAS)
    }

    // Calendar usa Domingo = 1; aqui a semana começa na segunda (Seg = 0)
    private fun diaDeHoje(): Int {
        val diaCalendar = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return (diaCalendar + 5) % 7
    }

    private fun montarDias() {
        val container = findViewById<LinearLayout>(R.id.containerDias)
        container.removeAllViews()
        abasDias.clear()

        dias.forEachIndexed { indice, nome ->
            val aba = layoutInflater.inflate(R.layout.item_dia_semana, container, false) as TextView
            aba.text = nome
            aba.setOnClickListener {
                diaSelecionado = indice
                atualizarAbas()
                montarLista()
            }
            container.addView(aba)
            abasDias.add(aba)
        }
        atualizarAbas()
    }

    private fun atualizarAbas() {
        abasDias.forEachIndexed { indice, aba ->
            val ativa = indice == diaSelecionado
            aba.setBackgroundResource(if (ativa) R.drawable.bg_tab_ativa else R.drawable.bg_tab_inativa)
            aba.setTextColor(
                ContextCompat.getColor(this, if (ativa) android.R.color.white else R.color.fm_muted)
            )
        }
    }

    private fun montarLista() {
        val container = findViewById<LinearLayout>(R.id.listaAulas)
        container.removeAllViews()

        val aulas = aulasPorDia[diaSelecionado].orEmpty()
        if (aulas.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Nenhuma aula neste dia."
            vazio.setTextColor(ContextCompat.getColor(this, R.color.fm_muted))
            vazio.textSize = 14f
            vazio.setPadding(0, 24, 0, 0)
            container.addView(vazio)
            return
        }

        for (aula in aulas) {
            val card = layoutInflater.inflate(R.layout.item_aula, container, false)
            card.findViewById<TextView>(R.id.txtNomeAula).text = aula.nome
            card.findViewById<TextView>(R.id.txtHorario).text = "${aula.inicio} - ${aula.fim}"
            card.findViewById<TextView>(R.id.txtInstrutor).text = aula.instrutor
            card.findViewById<TextView>(R.id.txtVagas).text = aula.vagas.toString()
            configurarBotao(card.findViewById(R.id.btnAcaoAula), aula)
            container.addView(card)
        }
    }

    // Inscrever (azul), Indisponível (cinza, sem vagas) ou Cancelar Inscrição (vermelho)
    private fun configurarBotao(botao: MaterialButton, aula: Aula) {
        when {
            aula.inscrito -> {
                botao.text = "Cancelar Inscrição"
                botao.isEnabled = true
                Dialogos.estilizar(this, botao, R.color.fm_red, true)
                botao.setOnClickListener { confirmarCancelamento(aula) }
            }
            aula.vagas == 0 -> {
                botao.text = "Indisponível"
                botao.isEnabled = false
                Dialogos.estilizar(this, botao, R.color.fm_border, true)
                botao.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            else -> {
                botao.text = "Inscrever"
                botao.isEnabled = true
                Dialogos.estilizar(this, botao, R.color.fm_blue, true)
                botao.setOnClickListener { confirmarInscricao(aula) }
            }
        }
    }

    private fun confirmarInscricao(aula: Aula) {
        Dialogos.confirmar(
            this,
            titulo = "Confirmar Inscrição?",
            mensagem = "Você está prestes a se inscrever na aula de ${aula.nome}, às ${aula.inicio}.",
            textoEsquerda = "Cancelar",
            textoDireita = "Confirmar"
        ) {
            // TODO: registrar a inscrição no backend
            aula.inscrito = true
            aula.vagas -= 1
            montarLista()
        }
    }

    private fun confirmarCancelamento(aula: Aula) {
        Dialogos.confirmar(
            this,
            titulo = "Cancelar Inscrição?",
            mensagem = "Você está prestes a cancelar sua vaga na aula de ${aula.nome}, às ${aula.inicio}.",
            textoEsquerda = "Sair",
            textoDireita = "Cancelar Reserva",
            corDireita = R.color.fm_red,
            esquerdaPreenchida = true
        ) {
            // TODO: cancelar a inscrição no backend
            aula.inscrito = false
            aula.vagas += 1
            montarLista()
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
