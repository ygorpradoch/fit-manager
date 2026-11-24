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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var edtEmail: EditText
    private lateinit var edtSenha: EditText
    private lateinit var txtErro: TextView
    private lateinit var btnEntrar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ajustarBarraDeStatus()

        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)
        txtErro = findViewById(R.id.txtErro)
        btnEntrar = findViewById(R.id.btnEntrar)

        btnEntrar.setOnClickListener { entrar() }

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

        txtErro.visibility = View.GONE

        if (email.isEmpty()) {
            edtEmail.error = "Preencha este campo"
            edtEmail.requestFocus()
            return
        }
        if (senha.isEmpty()) {
            edtSenha.error = "Preencha este campo"
            edtSenha.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtErro.visibility = View.VISIBLE
            return
        }

        btnEntrar.isEnabled = false

        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                abrirTelaInicial(resultado.user!!.uid)
            }
            .addOnFailureListener {
                btnEntrar.isEnabled = true
                txtErro.visibility = View.VISIBLE
            }
    }

    // busca o tipo do usuario no Firestore para saber se vai para a home do aluno ou do admin
    private fun abrirTelaInicial(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val tipo = doc.getString("tipo")
                val destino = if (tipo == "admin") AdminHomeActivity::class.java else HomeActivity::class.java
                startActivity(Intent(this, destino))
                finish()
            }
            .addOnFailureListener {
                btnEntrar.isEnabled = true
                txtErro.visibility = View.VISIBLE
            }
    }

    private fun ajustarBarraDeStatus() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
}
