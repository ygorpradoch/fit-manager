package com.example.fitmanager

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MeuContratoActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meu_contrato)
        ajustarBarraDeStatus()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnBaixarPdf).setOnClickListener { mostrarAvisoDownload() }

        carregarContrato()
    }

    // Busca o documento do aluno logado e preenche a tela
    private fun carregarContrato() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                texto(R.id.txtPlano, doc.getString("contrato.plano") ?: "Sem plano contratado")
                texto(R.id.txtAluno, doc.getString("nome") ?: "-")
                texto(R.id.txtMatricula, doc.getString("matricula") ?: "-")
                texto(R.id.txtInicio, doc.getString("contrato.inicio") ?: "-")
                texto(R.id.txtVigencia, doc.getString("contrato.vigencia") ?: "-")
                texto(R.id.txtValor, doc.getDouble("contrato.valorMensal")?.let { formatarMoeda(it) } ?: "-")

                val status = doc.getString("contrato.status") ?: "-"
                val ativo = status.equals("Ativo", ignoreCase = true)
                val etiqueta = findViewById<TextView>(R.id.txtStatus)
                etiqueta.text = status
                etiqueta.setTextColor(ContextCompat.getColor(this, if (ativo) R.color.fm_green else R.color.fm_red))
                etiqueta.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(if (ativo) "#DCFCE7" else "#FEE2E2"))
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível carregar o contrato", Toast.LENGTH_SHORT).show()
            }
    }


    private fun mostrarAvisoDownload() {
        AlertDialog.Builder(this)
            .setTitle("Meu Contrato")
            .setMessage("Download do contrato em PDF iniciado (simulação).")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun texto(id: Int, valor: String) {
        findViewById<TextView>(id).text = valor
    }

    private fun ajustarBarraDeStatus() {
        val header = findViewById<View>(R.id.headerContrato)
        val paddingTopOriginal = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, 0)
            header.updatePadding(top = paddingTopOriginal + bars.top)
            insets
        }
    }
}