package com.example.fitmanager

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class DetalheExercicioActivity : AppCompatActivity() {

    // Nomes das informações enviadas pela tela de Treinos
    companion object {
        const val EXTRA_NOME = "nome"
        const val EXTRA_GRUPO = "grupo"
        const val EXTRA_SERIES = "series"
        const val EXTRA_REPETICOES = "repeticoes"
        const val EXTRA_EQUIPAMENTOS = "equipamentos"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhe_exercicio)
        ajustarBarras()

        // Recebe os dados do exercício escolhido
        val nome = intent.getStringExtra(EXTRA_NOME).orEmpty()
        val grupo = intent.getStringExtra(EXTRA_GRUPO).orEmpty()
        val series = intent.getIntExtra(EXTRA_SERIES, 0)
        val repeticoes = intent.getIntExtra(EXTRA_REPETICOES, 0)
        val equipamentos = intent.getStringArrayListExtra(EXTRA_EQUIPAMENTOS) ?: arrayListOf()

        findViewById<TextView>(R.id.txtGrupoHeader).text = grupo
        findViewById<TextView>(R.id.txtNomeExercicio).text = nome
        findViewById<TextView>(R.id.txtResumo).text = "$grupo · $series séries · $repeticoes repetições"
        findViewById<TextView>(R.id.txtEquipamentos).text =
            equipamentos.joinToString("\n") { "• $it" }

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnDemonstracao).setOnClickListener { mostrarPopupDemonstracao() }

        BarraNavegacao.configurar(this, ItemNav.TREINOS)
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