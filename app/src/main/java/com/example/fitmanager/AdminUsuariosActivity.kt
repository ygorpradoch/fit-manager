package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class AdminUsuariosActivity : AppCompatActivity() {

    // Usuário de exemplo (sem banco por enquanto)
    private class Usuario(
        val nome: String,
        val matricula: String,
        var ativo: Boolean = true
    ) {
        val iniciais: String
            get() = nome.split(" ").filter { it.isNotEmpty() }.take(2)
                .joinToString("") { it.first().uppercase() }
    }

    private val usuarios = mutableListOf(
        Usuario("Matheus Lima", "2024001"),
        Usuario("Arthur Dio.", "2024002"),
        Usuario("Paulo Luan", "2024003")
    )

    private lateinit var edtBusca: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_usuarios)
        ajustarBarras()

        edtBusca = findViewById(R.id.edtBusca)

        // A seta leva de volta para a Home do admin, limpando as telas que estiverem por cima dela
        findViewById<View>(R.id.btnVoltar).setOnClickListener {
            val intent = Intent(this, AdminHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        edtBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = montarLista()
        })

        montarLista()
        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.USUARIOS)
    }

    // Filtra pelo texto da busca (nome ou matrícula) e monta um card por usuário
    private fun montarLista() {
        val container = findViewById<LinearLayout>(R.id.listaUsuarios)
        container.removeAllViews()

        val termo = edtBusca.text.toString().trim()
        val filtrados = usuarios.filter {
            it.nome.contains(termo, ignoreCase = true) || it.matricula.contains(termo)
        }

        if (filtrados.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Nenhum usuário encontrado."
            vazio.setTextColor(getColor(R.color.fm_muted))
            vazio.textSize = 14f
            vazio.setPadding(0, 24, 0, 0)
            container.addView(vazio)
            return
        }

        for (usuario in filtrados) {
            val card = layoutInflater.inflate(R.layout.item_usuario, container, false)
            card.findViewById<TextView>(R.id.txtIniciais).text = usuario.iniciais
            card.findViewById<TextView>(R.id.txtNomeUsuario).text = usuario.nome
            card.findViewById<TextView>(R.id.txtMatricula).text =
                if (usuario.ativo) "Matrícula ${usuario.matricula}"
                else "Matrícula ${usuario.matricula} · Desativado"
            card.findViewById<View>(R.id.btnMenuUsuario).setOnClickListener {
                mostrarMenu(usuario)
            }
            container.addView(card)
        }
    }

    private fun mostrarMenu(usuario: Usuario) {
        val dialog = Dialogos.criar(this, R.layout.dialog_menu_usuario)

        dialog.findViewById<TextView>(R.id.txtNomeMenu).text = usuario.nome
        dialog.findViewById<TextView>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<TextView>(R.id.btnDesativar).text =
            if (usuario.ativo) "Desativar Usuário" else "Reativar Usuário"

        dialog.findViewById<View>(R.id.btnMontarTreino).setOnClickListener {
            dialog.dismiss()
            // TODO: abrir a tela Montar Treino (issue #21) passando o aluno escolhido
            Toast.makeText(this, "Tela em desenvolvimento", Toast.LENGTH_SHORT).show()
        }

        dialog.findViewById<View>(R.id.btnDesativar).setOnClickListener {
            dialog.dismiss()
            if (usuario.ativo) confirmarDesativar(usuario) else reativar(usuario)
        }

        dialog.findViewById<View>(R.id.btnExcluir).setOnClickListener {
            dialog.dismiss()
            confirmarExcluir(usuario)
        }

        dialog.show()
    }

    private fun confirmarDesativar(usuario: Usuario) {
        Dialogos.confirmar(
            this,
            titulo = "Desativar Usuário?",
            mensagem = "O usuário perderá o acesso ao aplicativo até ser reativado.",
            textoEsquerda = "Cancelar",
            textoDireita = "Confirmar"
        ) {
            // TODO: desativar no backend
            usuario.ativo = false
            montarLista()
        }
    }

    private fun reativar(usuario: Usuario) {
        // TODO: reativar no backend
        usuario.ativo = true
        montarLista()
    }

    private fun confirmarExcluir(usuario: Usuario) {
        Dialogos.confirmar(
            this,
            titulo = "Excluir Conta?",
            mensagem = "Essa ação é permanente e remove todos os dados do usuário.",
            textoEsquerda = "Cancelar",
            textoDireita = "Confirmar",
            corDireita = R.color.fm_red
        ) {
            // TODO: excluir no backend
            usuarios.remove(usuario)
            montarLista()
        }
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerUsuarios)
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
