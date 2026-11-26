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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class TreinosActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private var uid = ""

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
        setContentView(R.layout.activity_treinos)
        ajustarBarras()

        // A seta leva de volta para a Home, limpando as telas que estiverem por cima dela
        findViewById<View>(R.id.btnVoltar).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        BarraNavegacao.configurar(this, ItemNav.TREINOS)
    }

    override fun onStart() {
        super.onStart()
        if (uid.isEmpty()) return
        observarTreinos()
    }

    override fun onStop() {
        super.onStop()
        ouvinte?.remove()
    }

    // Lê os treinos do aluno e atualiza a tela sempre que algo mudar no banco
    private fun observarTreinos() {
        ouvinte = TreinosRepositorio.observar(
            uid,
            aoMudar = { treinos -> montarLista(treinos) },
            aoErro = { erro ->
                Log.e("FIREBASE", "Erro ao ler os treinos", erro)
                Toast.makeText(this, "Não foi possível carregar os treinos", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Monta um card por treino (grupo muscular), com uma linha para cada exercício
    private fun montarLista(treinos: List<Treino>) {
        val container = findViewById<LinearLayout>(R.id.listaTreinos)
        container.removeAllViews()

        if (treinos.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Você ainda não tem treinos cadastrados."
            vazio.setTextColor(ContextCompat.getColor(this, R.color.fm_muted))
            vazio.setPadding(0, (24 * resources.displayMetrics.density).toInt(), 0, 0)
            container.addView(vazio)
            return
        }

        for (treino in treinos) {
            val card = layoutInflater.inflate(R.layout.item_grupo_treino, container, false)
            card.findViewById<TextView>(R.id.txtGrupo).text = treino.nome.uppercase(LOCALE_BR)
            val linhas = card.findViewById<LinearLayout>(R.id.containerExercicios)

            treino.exercicios.forEachIndexed { indice, exercicio ->
                val linha = layoutInflater.inflate(R.layout.item_exercicio_linha, linhas, false)
                linha.findViewById<TextView>(R.id.txtNomeExercicio).text = exercicio.nome
                linha.findViewById<TextView>(R.id.txtCarga).text = exercicio.resumoCarga

                // O último exercício do card fica sem divisor
                linha.findViewById<View>(R.id.divisor).visibility =
                    if (indice == treino.exercicios.lastIndex) View.GONE else View.VISIBLE

                linha.findViewById<View>(R.id.linhaExercicio).setOnClickListener {
                    abrirDetalhe(treino, indice)
                }
                linhas.addView(linha)
            }
            container.addView(card)
        }
    }

    // Envia o treino e a posição do exercício escolhido para a tela de detalhe
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
