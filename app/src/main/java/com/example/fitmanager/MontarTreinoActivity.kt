package com.example.fitmanager

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MontarTreinoActivity : AppCompatActivity() {

    // Nomes das informações enviadas pela tela de Gestão de Usuários
    companion object {
        const val EXTRA_NOME_ALUNO = "nome_aluno"
        const val EXTRA_MATRICULA = "matricula"
    }

    private val tipos = listOf("Musculação", "Funcional", "Cardio")
    private val dias = listOf(
        "Segunda e Quinta", "Terça e Sexta", "Segunda, Quarta e Sexta",
        "Terça e Quinta", "Sábado", "Todos os dias"
    )

    private lateinit var edtNomeTreino: EditText
    private lateinit var spTipo: Spinner
    private lateinit var spDias: Spinner
    private lateinit var containerExercicios: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_montar_treino)
        ajustarBarras()

        // Valores de exemplo caso a tela seja aberta direto, sem passar pela lista de usuários
        val nomeAluno = intent.getStringExtra(EXTRA_NOME_ALUNO) ?: "Matheus Lima"
        val matricula = intent.getStringExtra(EXTRA_MATRICULA) ?: "2024001"

        findViewById<TextView>(R.id.txtAluno).text = buildSpannedString {
            append("Aluno: ")
            bold { append(nomeAluno) }
            append(" · Matrícula $matricula")
        }

        edtNomeTreino = findViewById(R.id.edtNomeTreino)
        spTipo = findViewById(R.id.spTipo)
        spDias = findViewById(R.id.spDias)
        containerExercicios = findViewById(R.id.containerExerciciosForm)

        configurarSpinner(spTipo, tipos)
        configurarSpinner(spDias, dias)

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnCancelarTreino).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAdicionarExercicio).setOnClickListener { adicionarExercicio() }
        findViewById<View>(R.id.btnSalvarTreino).setOnClickListener { salvar() }

        adicionarExercicio()
    }

    private fun configurarSpinner(spinner: Spinner, itens: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, itens)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    // Adiciona um bloco de exercício vazio ao formulário
    private fun adicionarExercicio() {
        val item = layoutInflater.inflate(R.layout.item_exercicio_form, containerExercicios, false)
        containerExercicios.addView(item)

        // O primeiro exercício não pode ser removido
        if (containerExercicios.childCount > 1) {
            val remover = item.findViewById<TextView>(R.id.txtRemoverExercicio)
            remover.visibility = View.VISIBLE
            remover.setOnClickListener {
                containerExercicios.removeView(item)
                renumerarExercicios()
            }
        }
        renumerarExercicios()
    }

    private fun renumerarExercicios() {
        for (i in 0 until containerExercicios.childCount) {
            containerExercicios.getChildAt(i)
                .findViewById<TextView>(R.id.txtTituloExercicioForm).text = "Exercício ${i + 1}"
        }
    }

    private fun salvar() {
        var valido = true

        if (edtNomeTreino.text.toString().trim().isEmpty()) {
            edtNomeTreino.error = "Informe o nome do treino"
            valido = false
        }

        for (i in 0 until containerExercicios.childCount) {
            val nome = containerExercicios.getChildAt(i).findViewById<EditText>(R.id.edtNomeExercicio)
            if (nome.text.toString().trim().isEmpty()) {
                nome.error = "Informe o nome do exercício"
                valido = false
            }
        }

        if (!valido) return

        // TODO: salvar o treino no backend. Os dados estão em:
        //  edtNomeTreino, spTipo.selectedItem, spDias.selectedItem e, para cada filho de
        //  containerExercicios: edtNomeExercicio, edtSeries, edtRepeticoes, edtCarga, edtDescanso
        AlertDialog.Builder(this)
            .setMessage("Treino salvo com sucesso!")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }

    // Cabeçalho ocupa a barra de status; o rodapé acompanha o teclado
    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerMontarTreino)
        val topOriginal = header.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, 0, bars.right, maxOf(bars.bottom, teclado.bottom))
            header.updatePadding(top = topOriginal + bars.top)
            insets
        }
    }
}
