package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate

class HomeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var uid = ""

    private var treinos = listOf<Treino>()

    private var ouvinteUsuario: ListenerRegistration? = null
    private var ouvinteTreinos: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuario = auth.currentUser
        if (usuario == null) {
            irParaLogin()
            return
        }
        uid = usuario.uid

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ajustarBarras()

        findViewById<View>(R.id.fabGymBot).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        findViewById<View>(R.id.btnVerProximo1).setOnClickListener { abrirTreinos() }
        findViewById<View>(R.id.btnVerProximo2).setOnClickListener { abrirTreinos() }

        BarraNavegacao.configurar(this, ItemNav.HOME)
    }

    override fun onStart() {
        super.onStart()
        if (uid.isEmpty()) return
        observarUsuario()
        observarTreinos()
    }

    override fun onStop() {
        super.onStop()
        ouvinteUsuario?.remove()
        ouvinteTreinos?.remove()
    }

    private fun observarUsuario() {
        ouvinteUsuario = db.collection("usuarios").document(uid)
            .addSnapshotListener { doc, erro ->
                if (erro != null || doc == null) return@addSnapshotListener
                val primeiroNome = doc.getString("nome").orEmpty().trim().substringBefore(" ")
                findViewById<TextView>(R.id.txtSaudacao).text =
                    if (primeiroNome.isEmpty()) "Bem-vindo" else "Bem-vindo, $primeiroNome"
            }
    }

    private fun observarTreinos() {
        ouvinteTreinos = TreinosRepositorio.observar(
            uid,
            aoMudar = { lista ->
                treinos = lista
                renderizar()
            },
            aoErro = { erro ->
                Log.e("FIREBASE", "Erro ao ler os treinos", erro)
                Toast.makeText(this, "Não foi possível carregar os treinos", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun renderizar() {
        val hoje = LocalDate.now()
        montarTreinoDoDia(DiasTreino.treinoDoDia(treinos, hoje))
        montarProximos(DiasTreino.proximos(treinos, hoje))
    }

    private fun montarTreinoDoDia(treino: Treino?) {
        val titulo = findViewById<TextView>(R.id.txtTreinoDia)
        val aviso = findViewById<TextView>(R.id.txtSemTreino)
        val container = findViewById<LinearLayout>(R.id.containerTreinoDia)
        container.removeAllViews()

        if (treino == null) {
            titulo.text = "Treino do Dia"
            aviso.visibility = View.VISIBLE
            return
        }

        aviso.visibility = View.GONE
        titulo.text = "Treino do Dia: ${treino.nome.uppercase(LOCALE_BR)}"

        treino.exercicios.forEachIndexed { indice, exercicio ->
            val linha = layoutInflater.inflate(R.layout.item_exercicio_linha, container, false)
            linha.findViewById<TextView>(R.id.txtNomeExercicio).text = exercicio.nome
            linha.findViewById<TextView>(R.id.txtCarga).text = exercicio.resumoCarga

            // O último exercício do card fica sem divisor
            linha.findViewById<View>(R.id.divisor).visibility =
                if (indice == treino.exercicios.lastIndex) View.GONE else View.VISIBLE

            linha.findViewById<View>(R.id.linhaExercicio).setOnClickListener {
                abrirDetalhe(treino, indice)
            }
            container.addView(linha)
        }
    }

    private fun montarProximos(proximos: List<Treino>) {
        val visibilidade = if (proximos.isEmpty()) View.GONE else View.VISIBLE
        findViewById<View>(R.id.txtTituloProximos).visibility = visibilidade
        findViewById<View>(R.id.linhaProximos).visibility = visibilidade

        val cards = listOf(
            Pair(R.id.cardProximo1, R.id.txtProximo1),
            Pair(R.id.cardProximo2, R.id.txtProximo2)
        )

        cards.forEachIndexed { indice, (idCard, idTexto) ->
            val card = findViewById<View>(idCard)
            val treino = proximos.getOrNull(indice)

            if (treino == null) {
                card.visibility = View.INVISIBLE
            } else {
                card.visibility = View.VISIBLE
                findViewById<TextView>(idTexto).text = treino.nome
            }
        }
    }

    private fun abrirTreinos() {
        startActivity(Intent(this, TreinosActivity::class.java))
    }

    private fun abrirDetalhe(treino: Treino, indice: Int) {
        val intent = Intent(this, DetalheExercicioActivity::class.java)
        intent.putExtra(DetalheExercicioActivity.EXTRA_TREINO_ID, treino.id)
        intent.putExtra(DetalheExercicioActivity.EXTRA_INDICE, indice)
        startActivity(intent)
    }

    private fun irParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerHome)
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
