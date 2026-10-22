package com.example.fitmanager

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CadastroActivity : AppCompatActivity() {

    // enquanto nao tem backend, usa essa lista pra testar o e-mail repetido
    private val emailsCadastrados = listOf("carla@unifor.br", "marcelo@unifor.br")

    private lateinit var edtNome: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtSenha: EditText
    private lateinit var edtConfirmarSenha: EditText
    private lateinit var txtErro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)
        ajustarBarraDeStatus()

        edtNome = findViewById(R.id.edtNome)
        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha)
        txtErro = findViewById(R.id.txtErro)

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.txtLogin).setOnClickListener { finish() }
        findViewById<View>(R.id.btnCadastrar).setOnClickListener { cadastrar() }
    }


    private fun cadastrar() {
        val nome = edtNome.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val senha = edtSenha.text.toString()
        val confirmarSenha = edtConfirmarSenha.text.toString()

        val erro = when {
            nome.isEmpty() || email.isEmpty() || senha.isEmpty() -> "Preencha todos os campos"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Digite um e-mail válido"
            emailsCadastrados.contains(email.lowercase()) -> "Este e-mail já está cadastrado"
            !senhaValida(senha) -> "A senha precisa ter no mínimo 8 caracteres, um número e uma letra maiúscula"
            senha != confirmarSenha -> "As senhas não coincidem"
            else -> null
        }

        if (erro != null) {
            txtErro.text = erro
            txtErro.visibility = View.VISIBLE
            return
        }

        txtErro.visibility = View.GONE

        AlertDialog.Builder(this)
            .setMessage("Cadastro realizado com sucesso!")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }

    private fun senhaValida(senha: String): Boolean {
        return senha.length >= 8 && senha.any { it.isDigit() } && senha.any { it.isUpperCase() }
    }

    private fun ajustarBarraDeStatus() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
}
