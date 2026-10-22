package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtSenha: EditText
    private lateinit var txtErro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ajustarBarraDeStatus()

        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)
        txtErro = findViewById(R.id.txtErro)

        findViewById<View>(R.id.btnEntrar).setOnClickListener { entrar() }

        findViewById<View>(R.id.txtEsqueciSenha).setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }

        findViewById<View>(R.id.txtCadastrar).setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }


    private fun entrar() {
        val email = edtEmail.text.toString().trim()
        val senha = edtSenha.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || senha.isEmpty()) {
            txtErro.visibility = View.VISIBLE
            return
        }

        txtErro.visibility = View.GONE

        // TODO: validar no backend e mandar pra home do aluno ou do instrutor
        startActivity(Intent(this, PerfilActivity::class.java))
        finish()
    }

    private fun ajustarBarraDeStatus() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
}
