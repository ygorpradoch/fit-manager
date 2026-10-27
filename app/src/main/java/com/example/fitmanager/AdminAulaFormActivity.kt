package com.example.fitmanager

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding


class AdminAulaFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AULA = "aula"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_aula_form)
        ajustarBarras()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnCancelar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnSalvar).setOnClickListener { salvar() }

        val txtInicio = findViewById<TextView>(R.id.txtInicio)
        val txtFim = findViewById<TextView>(R.id.txtFim)
        txtInicio.setOnClickListener { escolherHorario(txtInicio) }
        txtFim.setOnClickListener { escolherHorario(txtFim) }

        preencherSeForEdicao()
    }


    @Suppress("DEPRECATION")
    private fun preencherSeForEdicao() {
        val aula = intent.getSerializableExtra(EXTRA_AULA) as? AulaAdmin ?: return

        findViewById<TextView>(R.id.txtTituloForm).text = "Editar Aula"
        findViewById<EditText>(R.id.edtNomeAula).setText(aula.nome)
        findViewById<TextView>(R.id.txtInicio).text = aula.inicio
        findViewById<TextView>(R.id.txtFim).text = aula.fim
        findViewById<EditText>(R.id.edtVagas).setText(aula.vagas.toString())

        selecionar(R.id.spnInstrutor, R.array.instrutores, aula.instrutor)
        selecionar(R.id.spnDia, R.array.dias_semana, aula.dia)
        selecionar(R.id.spnEquipamento, R.array.equipamentos_aula, aula.equipamento)
    }

    private fun selecionar(idSpinner: Int, idArray: Int, valor: String) {
        val opcoes = resources.getStringArray(idArray)
        findViewById<Spinner>(idSpinner).setSelection(opcoes.indexOf(valor).coerceAtLeast(0))
    }


    private fun escolherHorario(campo: TextView) {
        val partes = campo.text.toString().split(":")
        val hora = partes.getOrNull(0)?.toIntOrNull() ?: 8
        val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(this, { _, h, m ->
            campo.text = "%02d:%02d".format(h, m)
        }, hora, minuto, true).show()
    }


    private fun salvar() {
        val edtNome = findViewById<EditText>(R.id.edtNomeAula)
        if (edtNome.text.toString().isBlank()) {
            edtNome.error = "Preencha este campo"
            return
        }
        Toast.makeText(this, "Aula salva", Toast.LENGTH_SHORT).show()
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