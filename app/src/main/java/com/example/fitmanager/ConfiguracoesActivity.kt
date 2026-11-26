package com.example.fitmanager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ConfiguracoesActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

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

    // Popup "Alterar Senha": reautentica com a senha atual e define a nova
    private fun mostrarPopupAlterarSenha() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_alterar_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val edtAtual = dialog.findViewById<EditText>(R.id.edtSenhaAtual)
        val edtNova = dialog.findViewById<EditText>(R.id.edtNovaSenha)
        val edtConfirmar = dialog.findViewById<EditText>(R.id.edtConfirmarSenha)
        val btnAlterar = dialog.findViewById<View>(R.id.btnAlterarSenha)

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        btnAlterar.setOnClickListener {
            val atual = edtAtual.text.toString()
            val nova = edtNova.text.toString()
            val confirmar = edtConfirmar.text.toString()

            if (atual.isEmpty()) {
                edtAtual.error = "Preencha este campo"
                return@setOnClickListener
            }
            if (nova.isEmpty()) {
                edtNova.error = "Preencha este campo"
                return@setOnClickListener
            }
            if (!senhaValida(nova)) {
                edtNova.error = "Mínimo de 8 caracteres, um número e uma letra maiúscula"
                return@setOnClickListener
            }
            if (nova != confirmar) {
                edtConfirmar.error = "As senhas não coincidem"
                return@setOnClickListener
            }

            val usuario = auth.currentUser
            val email = usuario?.email
            if (usuario == null || email == null) {
                sair()
                return@setOnClickListener
            }

            btnAlterar.isEnabled = false
            val credencial = EmailAuthProvider.getCredential(email, atual)

            usuario.reauthenticate(credencial)
                .addOnSuccessListener {
                    usuario.updatePassword(nova)
                        .addOnSuccessListener {
                            dialog.dismiss()
                            mostrarAvisoSenhaAlterada()
                        }
                        .addOnFailureListener {
                            btnAlterar.isEnabled = true
                            Toast.makeText(this, "Não foi possível alterar a senha. Tente novamente", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnAlterar.isEnabled = true
                    if (e is FirebaseNetworkException) {
                        Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
                    } else {
                        edtAtual.error = "Senha atual incorreta"
                    }
                }
        }

        dialog.show()
    }

    private fun mostrarAvisoSenhaAlterada() {
        AlertDialog.Builder(this)
            .setMessage("Senha alterada com sucesso!")
            .setPositiveButton("OK", null)
            .show()
    }

    // Mesma regra de senha do cadastro: 8+ caracteres, um número e uma maiúscula
    private fun senhaValida(senha: String): Boolean {
        return senha.length >= 8 && senha.any { it.isDigit() } && senha.any { it.isUpperCase() }
    }

    // Encerra a sessão no Firebase e volta para o Login, limpando o histórico de telas
    private fun sair() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun ajustarBarraDeStatus() {
        val header = findViewById<View>(R.id.headerConfig)
        val paddingTopOriginal = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, 0)
            header.updatePadding(top = paddingTopOriginal + bars.top)
            insets
        }
    }
}