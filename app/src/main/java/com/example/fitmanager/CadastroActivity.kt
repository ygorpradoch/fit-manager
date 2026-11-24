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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var btnCadastrar: View
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
        btnCadastrar = findViewById(R.id.btnCadastrar)
        btnCadastrar.setOnClickListener { cadastrar() }
    }


    private fun cadastrar() {
        val nome = edtNome.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val senha = edtSenha.text.toString()
        val confirmarSenha = edtConfirmarSenha.text.toString()

        val erro = when {
            nome.isEmpty() || email.isEmpty() || senha.isEmpty() -> "Preencha todos os campos"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Digite um e-mail válido"
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
        btnCadastrar.isEnabled = false

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                salvarUsuario(resultado.user!!.uid, nome, email)
            }
            .addOnFailureListener { e ->
                btnCadastrar.isEnabled = true
                txtErro.text = if (e is FirebaseAuthUserCollisionException) {
                    "Este e-mail já está cadastrado"
                } else {
                    "Não foi possível cadastrar. Tente novamente"
                }
                txtErro.visibility = View.VISIBLE
            }
    }

    // o Auth guarda so e-mail e senha, entao nome e tipo ficam num documento em "usuarios"
    private fun salvarUsuario(uid: String, nome: String, email: String) {
        val usuario = hashMapOf(
            "nome" to nome,
            "email" to email,
            "tipo" to "aluno"
        )

        db.collection("usuarios").document(uid).set(usuario)
            .addOnSuccessListener {
                auth.signOut()
                AlertDialog.Builder(this)
                    .setMessage("Cadastro realizado com sucesso!")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
            }
            .addOnFailureListener {
                btnCadastrar.isEnabled = true
                txtErro.text = "Não foi possível salvar seus dados. Tente novamente"
                txtErro.visibility = View.VISIBLE
            }
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
