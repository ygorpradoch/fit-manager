package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ajustarBarras()

        // Botão flutuante: abre a tela do Gym Bot (feita pelo Ygor)
        findViewById<View>(R.id.fabGymBot).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }



        BarraNavegacao.configurar(this, ItemNav.HOME)
    }

    private fun avisarEmDesenvolvimento() {
        Toast.makeText(this, "Tela em desenvolvimento", Toast.LENGTH_SHORT).show()
    }

    // Cabeçalho ocupa a barra de status e a barra inferior respeita a navegação do sistema
    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerHome)
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