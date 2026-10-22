package com.example.fitmanager

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MeuContratoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meu_contrato)
        ajustarBarraDeStatus()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }

        findViewById<View>(R.id.btnBaixarPdf).setOnClickListener {
            mostrarAvisoDownload()
        }
    }

    private fun mostrarAvisoDownload() {
        AlertDialog.Builder(this)
            .setTitle("Meu Contrato")
            .setMessage("Download do contrato em PDF iniciado (simulação).")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun ajustarBarraDeStatus() {
        val header = findViewById<View>(R.id.headerContrato)
        val paddingTopOriginal = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            header.updatePadding(top = paddingTopOriginal + bars.top)
            insets
        }
    }
}