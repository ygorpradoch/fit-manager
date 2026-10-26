package com.example.fitmanager

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class AdminHomeActivity : AppCompatActivity() {

    companion object {
        // Nome do administrador (a tela de login pode enviar quando tiver backend)
        const val EXTRA_NOME = "nome"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_home)
        ajustarBarras()

        val nome = intent.getStringExtra(EXTRA_NOME) ?: "Marcelo"
        findViewById<TextView>(R.id.txtSaudacaoAdmin).text = "Bem-vindo, $nome"

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.HOME)
    }

    // Cabeçalho ocupa a barra de status e a barra inferior respeita a navegação do sistema
    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerAdmin)
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
