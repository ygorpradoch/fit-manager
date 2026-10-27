package com.example.fitmanager

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding


class AdminExercicioFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXERCICIO = "exercicio"
    }

    private lateinit var checkboxes: List<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_exercicio_form)
        ajustarBarras()

        checkboxes = listOf(
            findViewById(R.id.chkPeito),
            findViewById(R.id.chkCostas),
            findViewById(R.id.chkPernas),
            findViewById(R.id.chkBracos),
            findViewById(R.id.chkCore),
            findViewById(R.id.chkCardio),
            findViewById(R.id.chkCorpoInteiro),
            findViewById(R.id.chkFuncional)
        )

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnCancelar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSalvar).setOnClickListener { salvar() }

        preencherSeForEdicao()
    }


    @Suppress("DEPRECATION")
    private fun preencherSeForEdicao() {
        val exercicio = intent.getSerializableExtra(EXTRA_EXERCICIO) as? ExercicioAdmin ?: return

        findViewById<TextView>(R.id.txtTituloForm).text = "Editar Exercício"
        findViewById<EditText>(R.id.edtNomeExercicio).setText(exercicio.nome)
        findViewById<EditText>(R.id.edtInstrucoes).setText(exercicio.instrucoes)
        findViewById<EditText>(R.id.edtVideo).setText(exercicio.video)

        for (checkbox in checkboxes) {
            checkbox.isChecked = exercicio.categorias.contains(checkbox.text.toString())
        }

        selecionar(R.id.spnAparelho, R.array.aparelhos_exercicio, exercicio.aparelho)
        selecionar(R.id.spnDificuldade, R.array.dificuldades, exercicio.dificuldade)
    }

    private fun selecionar(idSpinner: Int, idArray: Int, valor: String) {
        val opcoes = resources.getStringArray(idArray)
        findViewById<Spinner>(idSpinner).setSelection(opcoes.indexOf(valor).coerceAtLeast(0))
    }


    private fun salvar() {
        val edtNome = findViewById<EditText>(R.id.edtNomeExercicio)
        if (edtNome.text.toString().isBlank()) {
            edtNome.error = "Preencha este campo"
            return
        }

        if (checkboxes.none { it.isChecked }) {
            Toast.makeText(this, "Escolha pelo menos uma categoria", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Exercício salvo", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerForm)
        val topOriginal = header.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            header.updatePadding(top = topOriginal + bars.top)
            insets
        }
    }
}
