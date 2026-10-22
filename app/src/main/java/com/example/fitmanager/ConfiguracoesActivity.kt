package com.example.fitmanager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class ConfiguracoesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracoes)
        ajustarBarraDeStatus()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }

        findViewById<View>(R.id.itemMeuContrato).setOnClickListener {
            startActivity(Intent(this, MeuContratoActivity::class.java))
        }

        findViewById<View>(R.id.itemAlterarSenha).setOnClickListener {
            mostrarPopupAlterarSenha()
        }

        findViewById<View>(R.id.itemSair).setOnClickListener {
            sair()
        }
    }


    private fun mostrarPopupAlterarSenha() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_alterar_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<View>(R.id.btnAlterarSenha).setOnClickListener {
            dialog.dismiss()
            mostrarAvisoSenhaAlterada()
        }

        dialog.show()
    }


    private fun mostrarAvisoSenhaAlterada() {
        AlertDialog.Builder(this)
            .setMessage("Senha alterada com sucesso!")
            .setPositiveButton("OK", null)
            .show()
    }


    private fun sair() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun ajustarBarraDeStatus() {
        val header = findViewById<View>(R.id.headerConfig)
        val paddingTopOriginal = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            header.updatePadding(top = paddingTopOriginal + bars.top)
            insets
        }
    }
}