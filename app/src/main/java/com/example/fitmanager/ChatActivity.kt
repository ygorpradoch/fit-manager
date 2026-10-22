package com.example.fitmanager

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class ChatActivity : AppCompatActivity() {

    private lateinit var scrollChat: ScrollView
    private lateinit var layoutMensagens: LinearLayout
    private lateinit var edtMensagem: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ajustarBarraDeStatus()

        scrollChat = findViewById(R.id.scrollChat)
        layoutMensagens = findViewById(R.id.layoutMensagens)
        edtMensagem = findViewById(R.id.edtMensagem)

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnEnviar).setOnClickListener { enviarMensagem() }

        edtMensagem.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                enviarMensagem()
                true
            } else {
                false
            }
        }

        adicionarMensagem("Olá, você está falando com o Gym Bot! Como posso te ajudar hoje?", false)
    }


    private fun enviarMensagem() {
        val texto = edtMensagem.text.toString().trim()
        if (texto.isEmpty()) return

        adicionarMensagem(texto, true)
        edtMensagem.text.clear()

        layoutMensagens.postDelayed({
            adicionarMensagem(responder(texto), false)
        }, 600)
    }

    // respostas fixas por enquanto, depois troca pela integracao com a IA
    private fun responder(pergunta: String): String {
        val texto = pergunta.lowercase()
        return when {
            texto.contains("treino") -> "Hoje você tem treino de perna. Gostaria de mais detalhes?"
            texto.contains("aula") -> "Você pode ver as aulas do dia e se inscrever na aba Aulas."
            texto.contains("senha") -> "Para trocar a senha vá em Perfil > Configurações > Alterar Senha."
            else -> "Não entendi sua pergunta. Poderia reformular?"
        }
    }

    private fun adicionarMensagem(texto: String, doUsuario: Boolean) {
        val layout = if (doUsuario) R.layout.item_mensagem_usuario else R.layout.item_mensagem_bot
        val balao = layoutInflater.inflate(layout, layoutMensagens, false) as TextView
        balao.text = texto
        layoutMensagens.addView(balao)

        scrollChat.post { scrollChat.fullScroll(View.FOCUS_DOWN) }
    }

    private fun ajustarBarraDeStatus() {
        val header = findViewById<View>(R.id.headerChat)
        val paddingTopOriginal = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, 0, bars.right, maxOf(bars.bottom, teclado.bottom))
            header.updatePadding(top = paddingTopOriginal + bars.top)
            insets
        }
    }
}
