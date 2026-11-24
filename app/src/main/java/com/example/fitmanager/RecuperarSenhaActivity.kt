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

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var txtErro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_senha)
        ajustarBarraDeStatus()

        edtEmail = findViewById(R.id.edtEmail)
        txtErro = findViewById(R.id.txtErro)

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnEnviarLink).setOnClickListener { enviarLink() }
    }


    private fun enviarLink() {
        val email = edtEmail.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtErro.text = "Digite um e-mail válido"
            txtErro.visibility = View.VISIBLE
            return
        }

        txtErro.visibility = View.GONE

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setMessage("Se o e-mail estiver cadastrado, você receberá o link de recuperação")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
            }
            .addOnFailureListener {
                txtErro.text = "Não foi possível enviar o link. Tente novamente"
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
