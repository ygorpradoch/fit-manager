package com.example.fitmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class AdminExercicioDetalheActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXERCICIO = "exercicio"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_exercicio_detalhe)
        ajustarBarras()

        val exercicio = lerExercicio()
        if (exercicio == null) {
            finish()
            return
        }
        preencher(exercicio)

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnVideo).setOnClickListener { abrirVideo(exercicio.video) }

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.EXERCICIOS)
    }

    @Suppress("DEPRECATION")
    private fun lerExercicio(): ExercicioAdmin? =
        intent.getSerializableExtra(EXTRA_EXERCICIO) as? ExercicioAdmin


    private fun preencher(exercicio: ExercicioAdmin) {
        findViewById<TextView>(R.id.txtTituloDetalhe).text = exercicio.nome
        findViewById<TextView>(R.id.txtInstrucoes).text = exercicio.instrucoes
        findViewById<TextView>(R.id.txtEquipamentos).text = "• ${exercicio.aparelho}"
        findViewById<TextView>(R.id.txtCategorias).text = exercicio.categorias.joinToString(", ")

        val txtDificuldade = findViewById<TextView>(R.id.txtDificuldade)
        txtDificuldade.text = exercicio.dificuldade

        // iniciante fica verde, o resto fica amarelo
        if (exercicio.dificuldade == "Iniciante") {
            txtDificuldade.setBackgroundResource(R.drawable.bg_badge_ativo)
            txtDificuldade.setTextColor(ContextCompat.getColor(this, R.color.fm_green))
        }
    }

    private fun abrirVideo(link: String) {
        if (link.isBlank()) {
            Toast.makeText(this, "Esse exercício não tem vídeo cadastrado", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerDetalhe)
        val bottomNav = findViewById<View>(R.id.bottomNavAdmin)
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
