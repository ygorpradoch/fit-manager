package com.example.fitmanager

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Serve para Adicionar Aula (aulaId vazio) e para Editar Aula (aulaId recebido)
class AdminAulaFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AULA_ID = "aulaId"
    }

    private val db = FirebaseFirestore.getInstance()
    private var aulaId: String? = null
    private var aulaOriginal: AulaAdmin? = null

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

        aulaId = intent.getStringExtra(EXTRA_AULA_ID)
        val id = aulaId
        if (id != null) carregarAulaParaEdicao(id)
    }

    // Edição: busca a aula, muda o título e preenche os campos
    private fun carregarAulaParaEdicao(id: String) {
        findViewById<TextView>(R.id.txtTituloForm).text = "Editar Aula"
        val btnSalvar = findViewById<View>(R.id.btnSalvar)
        btnSalvar.isEnabled = false

        db.collection("aulas").document(id).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Aula não encontrada", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
                val aula = AulaAdmin.de(doc)
                aulaOriginal = aula
                preencher(aula)
                btnSalvar.isEnabled = true
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "Erro ao carregar a aula", e)
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun preencher(aula: AulaAdmin) {
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

    // Abre o relógio para escolher o horário do campo tocado
    private fun escolherHorario(campo: TextView) {
        val partes = campo.text.toString().split(":")
        val hora = partes.getOrNull(0)?.toIntOrNull() ?: 8
        val minuto = partes.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(this, { _, h, m ->
            campo.text = "%02d:%02d".format(h, m)
        }, hora, minuto, true).show()
    }

    // Valida o formulário e grava no Firestore (cria ou atualiza)
    private fun salvar() {
        val edtNome = findViewById<EditText>(R.id.edtNomeAula)
        val edtVagas = findViewById<EditText>(R.id.edtVagas)
        val btnSalvar = findViewById<View>(R.id.btnSalvar)

        val nome = edtNome.text.toString().trim()
        val vagas = edtVagas.text.toString().trim().toIntOrNull()
        val inicio = findViewById<TextView>(R.id.txtInicio).text.toString()
        val fim = findViewById<TextView>(R.id.txtFim).text.toString()

        if (nome.isEmpty()) {
            edtNome.error = "Preencha este campo"
            return
        }
        if (vagas == null || vagas <= 0) {
            edtVagas.error = "Digite um número de vagas maior que zero"
            return
        }
        val original = aulaOriginal
        if (original != null && vagas < original.matriculados) {
            edtVagas.error = "Já existem ${original.matriculados} alunos matriculados"
            return
        }
        if (horaParaMinutos(fim) <= horaParaMinutos(inicio)) {
            Toast.makeText(this, "O horário de fim deve ser depois do início", Toast.LENGTH_SHORT).show()
            return
        }

        val dados = hashMapOf<String, Any>(
            "nome" to nome,
            "instrutor" to findViewById<Spinner>(R.id.spnInstrutor).selectedItem.toString(),
            "dia" to findViewById<Spinner>(R.id.spnDia).selectedItem.toString(),
            "equipamento" to findViewById<Spinner>(R.id.spnEquipamento).selectedItem.toString(),
            "inicio" to inicio,
            "fim" to fim,
            "vagas" to vagas
        )

        btnSalvar.isEnabled = false
        val id = aulaId

        if (id == null) {
            // Aula nova: começa sem alunos matriculados
            dados["matriculados"] = 0
            dados["criadoEm"] = FieldValue.serverTimestamp()
            db.collection("aulas").add(dados)
                .addOnSuccessListener { aoSalvar() }
                .addOnFailureListener { e -> aoFalhar(e) }
        } else {
            db.collection("aulas").document(id).update(dados)
                .addOnSuccessListener { aoSalvar() }
                .addOnFailureListener { e -> aoFalhar(e) }
        }
    }

    private fun aoSalvar() {
        Toast.makeText(this, "Aula salva", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun aoFalhar(e: Exception) {
        findViewById<View>(R.id.btnSalvar).isEnabled = true
        Log.e("FIREBASE", "Erro ao salvar a aula", e)
        Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
    }

    // Considera também o teclado, para ele não cobrir os campos
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