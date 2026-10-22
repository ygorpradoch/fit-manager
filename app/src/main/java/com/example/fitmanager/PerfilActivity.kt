package com.example.fitmanager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ajustarBarras()

        findViewById<View>(R.id.btnConfiguracoes).setOnClickListener {
            startActivity(Intent(this, ConfiguracoesActivity::class.java))
        }

        findViewById<View>(R.id.btnAdicionarObjetivo).setOnClickListener {
            mostrarPopupAdicionarObjetivo()
        }

        findViewById<View>(R.id.btnEditarPerfil).setOnClickListener {
            mostrarPopupEditarPerfil()
        }

        BarraNavegacao.configurar(this, ItemNav.PERFIL)
    }


    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerPerfil)
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


    private fun mostrarPopupAdicionarObjetivo() {
        val dialog = criarDialog(R.layout.dialog_adicionar_objetivo)

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnConfirmarObjetivo).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }


    private fun mostrarPopupEditarPerfil() {
        val dialog = criarDialog(R.layout.dialog_editar_perfil)

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnSalvar).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }


    private fun criarDialog(layout: Int): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }
}