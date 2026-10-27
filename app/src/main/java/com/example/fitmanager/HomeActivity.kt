package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
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

        findViewById<View>(R.id.fabGymBot).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        findViewById<View>(R.id.btnVerPeito).setOnClickListener { abrirTreinos() }
        findViewById<View>(R.id.btnVerCostas).setOnClickListener { abrirTreinos() }

        BarraNavegacao.configurar(this, ItemNav.HOME)
    }

    private fun abrirTreinos() {
        startActivity(Intent(this, TreinosActivity::class.java))
    }
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