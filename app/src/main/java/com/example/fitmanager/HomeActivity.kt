package com.example.fitmanager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class DetalheExercicioActivity : AppCompatActivity() {

    // Informações enviadas pelas telas de Home e Treinos: qual treino e qual exercício dele
    companion object {
        const val EXTRA_TREINO_ID = "treino_id"
        const val EXTRA_INDICE = "indice"
    }

    private val auth = FirebaseAuth.getInstance()
    private var uid = ""
    private var treinoId = ""
    private var indice = -1

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

        // Sem saber qual exercício abrir, não há o que mostrar
        treinoId = intent.getStringExtra(EXTRA_TREINO_ID).orEmpty()
        indice = intent.getIntExtra(EXTRA_INDICE, -1)
        if (treinoId.isEmpty() || indice < 0) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe_exercicio)
        ajustarBarras()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnDemonstracao).setOnClickListener { mostrarPopupDemonstracao() }

        BarraNavegacao.configurar(this, ItemNav.TREINOS)
    }

    override fun onStart() {
        super.onStart()
        if (uid.isEmpty() || treinoId.isEmpty()) return
        observarTreino()
    }

    override fun onStop() {
        super.onStop()
        ouvinte?.remove()
    }

    // Lê o treino no Firestore e mostra o exercício escolhido (atualiza se o instrutor mudar algo)
    private fun observarTreino() {
        ouvinte = TreinosRepositorio.observarTreino(
            uid,
            treinoId,
            aoMudar = { treino -> mostrarExercicio(treino) },
            aoErro = { erro ->
                Log.e("FIREBASE", "Erro ao ler o exercício", erro)
                Toast.makeText(this, "Não foi possível carregar o exercício", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun mostrarExercicio(treino: Treino?) {
        val exercicio = treino?.exercicios?.getOrNull(indice)

        // O treino ou o exercício deixou de existir (ex.: o instrutor removeu)
        if (treino == null || exercicio == null) {
            Toast.makeText(this, "Este exercício não está mais disponível", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.txtGrupoHeader).text = treino.nome
        findViewById<TextView>(R.id.txtNomeExercicio).text = exercicio.nome
        findViewById<TextView>(R.id.txtResumo).text =
            "${treino.nome} · ${exercicio.series} séries · ${exercicio.repeticoes} repetições"
        findViewById<TextView>(R.id.txtEquipamentos).text =
            if (exercicio.equipamentos.isEmpty()) {
                "Nenhum equipamento informado"
            } else {
                exercicio.equipamentos.joinToString("\n") { "• $it" }
            }
    }

    // Popup informando onde o vídeo do YouTube seria exibido
    private fun mostrarPopupDemonstracao() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_demonstracao)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<View>(R.id.btnFecharDemo).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun irParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerDetalhe)
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
