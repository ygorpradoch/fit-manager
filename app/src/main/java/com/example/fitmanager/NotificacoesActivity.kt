package com.example.fitmanager

import android.content.Intent
import android.content.res.ColorStateList
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
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration

// Uma notificação guardada em usuarios/{uid}/notificacoes/{id}
data class Notificacao(
    val id: String,
    val titulo: String,
    val descricao: String,
    val criadoEm: Date?,
    val lida: Boolean
)

class NotificacoesActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var uid = ""

    private var notificacoes = listOf<Notificacao>()
    private var mostrandoNaoLidas = false
    private var criandoExemplos = false

    private var ouvinte: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sem usuário logado, volta para o Login
        val usuario = auth.currentUser
        if (usuario == null) {
            irParaLogin()
            return
        }
        uid = usuario.uid

        enableEdgeToEdge()
        setContentView(R.layout.activity_notificacoes)
        ajustarBarras()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.tabTodas).setOnClickListener { selecionarAba(false) }
        findViewById<View>(R.id.tabNaoLidas).setOnClickListener { selecionarAba(true) }

        BarraNavegacao.configurar(this, ItemNav.NOTIFICACOES)
        selecionarAba(false)

    }

    override fun onStart() {
        super.onStart()
        if (uid.isEmpty()) return
        observarNotificacoes()
    }

    override fun onStop() {
        super.onStop()
        ouvinte?.remove()
    }

    private fun colecao(): CollectionReference =
        db.collection("usuarios").document(uid).collection("notificacoes")

    // Lê as notificações do aluno e atualiza a tela sempre que algo mudar no banco
    private fun observarNotificacoes() {
        ouvinte = colecao().addSnapshotListener { resultado, erro ->
            if (erro != null) {
                Log.e("FIREBASE", "Erro ao ler as notificações", erro)
                Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            if (resultado == null) return@addSnapshotListener

            // Banco confirmou que está vazio: cria as notificações de exemplo (só demonstração)
            if (resultado.isEmpty && !resultado.metadata.isFromCache) {
                criarNotificacoesDeExemplo()
                return@addSnapshotListener
            }

            // RF09.4: das mais recentes para as mais antigas
            notificacoes = resultado.documents.map { d ->
                Notificacao(
                    id = d.id,
                    titulo = d.getString("titulo").orEmpty(),
                    descricao = d.getString("descricao").orEmpty(),
                    criadoEm = d.getTimestamp("criadoEm")?.toDate(),
                    lida = d.getBoolean("lida") ?: false
                )
            }.sortedByDescending { it.criadoEm?.time ?: 0L }

            renderizarLista()
        }
    }

    // Troca entre as abas "Todas" e "Não lidas"
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

    // Monta os cards conforme a aba selecionada
    private fun renderizarLista() {
        val container = findViewById<LinearLayout>(R.id.listaNotificacoes)
        container.removeAllViews()

        val lista = if (mostrandoNaoLidas) notificacoes.filter { !it.lida } else notificacoes

        if (lista.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = if (mostrandoNaoLidas) {
                "Você não tem notificações não lidas."
            } else {
                "Você ainda não tem notificações."
            }
            vazio.setTextColor(ContextCompat.getColor(this, R.color.fm_muted))
            vazio.setPadding(0, 24, 0, 0)
            container.addView(vazio)
            return
        }

        for (notificacao in lista) {
            val card = layoutInflater.inflate(R.layout.item_notificacao, container, false) as MaterialCardView

            card.findViewById<TextView>(R.id.txtTitulo).text = notificacao.titulo
            card.findViewById<TextView>(R.id.txtDescricao).text = notificacao.descricao
            card.findViewById<TextView>(R.id.txtTempo).text = formatarTempoDecorrido(notificacao.criadoEm)
            aplicarEstado(card, notificacao.lida)

            // RF09.3: ao tocar numa não lida, ela passa a ser lida
            card.setOnClickListener {
                if (!notificacao.lida) marcarComoLida(notificacao)
            }

            container.addView(card)
        }
    }

    // Grava lida = true no Firestore; a tela se atualiza sozinha pelo listener
    private fun marcarComoLida(notificacao: Notificacao) {
        colecao().document(notificacao.id).update("lida", true)
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível marcar como lida", Toast.LENGTH_SHORT).show()
            }
    }

    // Visual de lida (cinza) ou não lida (destaque azul)
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

    // APENAS PARA DEMONSTRAÇÃO: cria notificações de exemplo para quem ainda não tem nenhuma.
    // REMOVER quando as notificações passarem a ser geradas pelo sistema.

    private fun criarNotificacoesDeExemplo() {
        if (criandoExemplos) return
        criandoExemplos = true

        val agora = System.currentTimeMillis()
        val hora = 3_600_000L
        val dia = 24 * hora

        val exemplos: List<Map<String, Any>> = listOf(
            mapOf(
                "titulo" to "Treino de perna hoje",
                "descricao" to "Faça seu exercício diário",
                "criadoEm" to Timestamp(Date(agora - 2 * hora)),
                "lida" to false
            ),
            mapOf(
                "titulo" to "Treino de peito hoje",
                "descricao" to "Faça seu exercício diário",
                "criadoEm" to Timestamp(Date(agora - dia)),
                "lida" to true
            ),
            mapOf(
                "titulo" to "Treino de costas hoje",
                "descricao" to "Faça seu exercício diário",
                "criadoEm" to Timestamp(Date(agora - 2 * dia)),
                "lida" to true
            ),
            mapOf(
                "titulo" to "Conheça o crossfit",
                "descricao" to "Faça uma aula experimental",
                "criadoEm" to Timestamp(Date(agora - 2 * dia - hora)),
                "lida" to false
            ),
            mapOf(
                "titulo" to "Verifique sua conta",
                "descricao" to "Verificação de conta",
                "criadoEm" to Timestamp(Date(agora - 3 * dia)),
                "lida" to true
            )
        )

        // Cria um lote de gravação no Firestore
        val lote = db.batch()

        // Adiciona cada notificação ao lote
        exemplos.forEach { exemplo ->
            val documento = colecao().document()
            lote.set(documento, exemplo)
        }

        // Salva todas as notificações
        lote.commit().addOnSuccessListener {
            Log.d("FIREBASE", "Notificações criadas com sucesso")
        }.addOnFailureListener { e ->
            criandoExemplos = false
            Log.e("FIREBASE", "Erro ao criar notificações", e)
            Toast.makeText(
                this,
                "Erro: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun irParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Cabeçalho ocupa a barra de status e a barra inferior respeita a navegação do sistema
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