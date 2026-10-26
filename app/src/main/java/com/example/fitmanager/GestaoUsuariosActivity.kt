package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import com.google.android.material.button.MaterialButton

// Modelo de um usuário (dados de exemplo, sem banco por enquanto)
data class Usuario(
    val nome: String,
    val matricula: String,
    var ativo: Boolean = true
) {
    // "Matheus Lima" -> "ML"
    val iniciais: String
        get() = nome.trim().split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}

class GestaoUsuariosActivity : AppCompatActivity() {

    private val usuarios = mutableListOf(
        Usuario("Matheus Lima", "2024001"),
        Usuario("Arthur Dio.", "2024002"),
        Usuario("Paulo Luan", "2024003")
    )

    private lateinit var edtBusca: EditText
    private lateinit var listaUsuarios: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestao_usuarios)
        ajustarBarras()

        edtBusca = findViewById(R.id.edtBusca)
        listaUsuarios = findViewById(R.id.listaUsuarios)

        // A seta leva de volta para o painel, limpando as telas que estiverem por cima dele
        findViewById<View>(R.id.btnVoltar).setOnClickListener {
            val intent = Intent(this, AdminHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        edtBusca.doOnTextChanged { _, _, _, _ -> montarLista() }

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.USUARIOS)
        montarLista()
    }

    // Monta um card por usuário, aplicando o filtro da busca (nome ou matrícula)
    private fun montarLista() {
        listaUsuarios.removeAllViews()
        val filtro = edtBusca.text.toString().trim()

        val filtrados = usuarios.filter {
            filtro.isEmpty() ||
                it.nome.contains(filtro, ignoreCase = true) ||
                it.matricula.contains(filtro)
        }

        if (filtrados.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Nenhum usuário encontrado."
            vazio.setTextColor(ContextCompat.getColor(this, R.color.fm_muted))
            vazio.textSize = 14f
            vazio.setPadding(0, (24 * resources.displayMetrics.density).toInt(), 0, 0)
            listaUsuarios.addView(vazio)
            return
        }

        for (usuario in filtrados) {
            val card = layoutInflater.inflate(R.layout.item_usuario, listaUsuarios, false)

            card.findViewById<TextView>(R.id.txtAvatarUsuario).text = usuario.iniciais
            card.findViewById<TextView>(R.id.txtNomeUsuario).text = usuario.nome
            card.findViewById<TextView>(R.id.txtMatriculaUsuario).text =
                if (usuario.ativo) "Matrícula ${usuario.matricula}"
                else "Matrícula ${usuario.matricula} · Desativado"

            // Usuário desativado fica mais apagado
            card.alpha = if (usuario.ativo) 1f else 0.55f

            card.findViewById<View>(R.id.btnMenuUsuario).setOnClickListener {
                mostrarPopupAcoes(usuario)
            }
            listaUsuarios.addView(card)
        }
    }

    // Popup com as ações possíveis para o usuário escolhido
    private fun mostrarPopupAcoes(usuario: Usuario) {
        val dialog = Dialogos.criar(this, R.layout.dialog_acoes_usuario)

        dialog.findViewById<TextView>(R.id.txtNomeDialogo).text = usuario.nome
        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        val btnDesativar = dialog.findViewById<MaterialButton>(R.id.btnDesativarUsuario)
        btnDesativar.text = if (usuario.ativo) "Desativar Usuário" else "Reativar Usuário"

        dialog.findViewById<View>(R.id.btnMontarTreino).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MontarTreinoActivity::class.java)
            intent.putExtra(MontarTreinoActivity.EXTRA_NOME_ALUNO, usuario.nome)
            intent.putExtra(MontarTreinoActivity.EXTRA_MATRICULA, usuario.matricula)
            startActivity(intent)
        }

        btnDesativar.setOnClickListener {
            dialog.dismiss()
            if (usuario.ativo) {
                confirmarDesativacao(usuario)
            } else {
                usuario.ativo = true
                montarLista()
                Toast.makeText(this, "Usuário reativado.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.findViewById<View>(R.id.btnExcluirConta).setOnClickListener {
            dialog.dismiss()
            confirmarExclusao(usuario)
        }

        dialog.show()
    }

    private fun confirmarDesativacao(usuario: Usuario) {
        Dialogos.confirmar(
            activity = this,
            titulo = "Desativar Usuário?",
            mensagem = "O usuário perderá o acesso ao aplicativo até ser reativado.",
            textoEsquerda = "Cancelar",
            estiloEsquerda = EstiloBotao.CONTORNO,
            textoDireita = "Confirmar",
            estiloDireita = EstiloBotao.PERIGO
        ) {
            usuario.ativo = false
            montarLista()
            Toast.makeText(this, "Usuário desativado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarExclusao(usuario: Usuario) {
        Dialogos.confirmar(
            activity = this,
            titulo = "Excluir Conta?",
            mensagem = "Esta ação é permanente e não poderá ser desfeita.",
            textoEsquerda = "Cancelar",
            estiloEsquerda = EstiloBotao.CONTORNO,
            textoDireita = "Excluir",
            estiloDireita = EstiloBotao.PERIGO
        ) {
            usuarios.remove(usuario)
            montarLista()
            Toast.makeText(this, "Conta excluída.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerGestao)
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
