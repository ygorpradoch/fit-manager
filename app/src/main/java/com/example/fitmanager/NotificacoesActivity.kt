package com.example.fitmanager

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.card.MaterialCardView


data class Notificacao(
    val titulo: String,
    val descricao: String,
    val tempo: String,
    var lida: Boolean
)

class NotificacoesActivity : AppCompatActivity() {


    private val notificacoes = listOf(
        Notificacao("Treino de perna hoje", "Faça seu exercício diário", "Há 2 horas", false),
        Notificacao("Treino de peito hoje", "Faça seu exercício diário", "Há 1 dia", true),
        Notificacao("Treino de costas hoje", "Faça seu exercício diário", "Há 2 dias", true),
        Notificacao("Conheça o crossfit", "Faça uma aula experimental", "Há 2 dias", false),
        Notificacao("Verifique sua conta", "Verificação de conta", "Há 3 dias", true)
    )

    private var mostrandoNaoLidas = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notificacoes)
        ajustarBarras()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() } // TODO: voltar para a Home
        findViewById<View>(R.id.tabTodas).setOnClickListener { selecionarAba(false) }
        findViewById<View>(R.id.tabNaoLidas).setOnClickListener { selecionarAba(true) }

        BarraNavegacao.configurar(this, ItemNav.NOTIFICACOES)
        selecionarAba(false)
    }


    private fun selecionarAba(naoLidas: Boolean) {
        mostrandoNaoLidas = naoLidas
        estilizarAba(findViewById(R.id.tabTodas), !naoLidas)
        estilizarAba(findViewById(R.id.tabNaoLidas), naoLidas)
        renderizarLista()
    }

    private fun estilizarAba(aba: TextView, ativa: Boolean) {
        aba.setBackgroundResource(if (ativa) R.drawable.bg_tab_ativa else R.drawable.bg_tab_inativa)
        aba.setTextColor(
            ContextCompat.getColor(this, if (ativa) android.R.color.white else R.color.fm_muted)
        )
    }


    private fun renderizarLista() {
        val container = findViewById<LinearLayout>(R.id.listaNotificacoes)
        container.removeAllViews()

        val lista = if (mostrandoNaoLidas) notificacoes.filter { !it.lida } else notificacoes

        for (notificacao in lista) {
            val card = LayoutInflater.from(this)
                .inflate(R.layout.item_notificacao, container, false) as MaterialCardView

            card.findViewById<TextView>(R.id.txtTitulo).text = notificacao.titulo
            card.findViewById<TextView>(R.id.txtDescricao).text = notificacao.descricao
            card.findViewById<TextView>(R.id.txtTempo).text = notificacao.tempo
            aplicarEstado(card, notificacao.lida)


            card.setOnClickListener {
                notificacao.lida = true
                renderizarLista()
            }

            container.addView(card)
        }
    }


    private fun aplicarEstado(card: MaterialCardView, lida: Boolean) {
        val corTitulo = ContextCompat.getColor(this, if (lida) R.color.fm_text_lida else R.color.fm_text)
        val corTexto = ContextCompat.getColor(this, if (lida) R.color.fm_text_lida else R.color.fm_muted)
        val corPonto = ContextCompat.getColor(this, if (lida) R.color.fm_text_lida else R.color.fm_blue)
        val corCard = ContextCompat.getColor(this, if (lida) R.color.fm_card_lida else android.R.color.white)

        card.setCardBackgroundColor(corCard)
        card.findViewById<TextView>(R.id.txtTitulo).setTextColor(corTitulo)
        card.findViewById<TextView>(R.id.txtDescricao).setTextColor(corTexto)
        card.findViewById<TextView>(R.id.txtTempo).setTextColor(corTexto)
        card.findViewById<View>(R.id.pontoStatus).backgroundTintList = ColorStateList.valueOf(corPonto)
    }


    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerNotificacoes)
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