package com.example.fitmanager

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged


data class Aparelho(
    val nome: String,
    val tipo: String,
    var status: String
)

class AdminAparelhosActivity : AppCompatActivity() {

    private val aparelhos = mutableListOf(
        Aparelho("Esteira", "Cardio", "Operacional"),
        Aparelho("Leg Press", "Musculação", "Operacional"),
        Aparelho("Cadeira Extensora", "Musculação", "Manutenção"),
        Aparelho("Bicicleta", "Cardio", "Operacional")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_aparelhos)
        ajustarBarras()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnAdicionar).setOnClickListener { mostrarPopupAdicionar() }
        findViewById<EditText>(R.id.edtBuscar).doAfterTextChanged { renderizarLista() }

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.APARELHOS)
        renderizarLista()
    }


    private fun renderizarLista() {
        val container = findViewById<LinearLayout>(R.id.listaAparelhos)
        val filtro = findViewById<EditText>(R.id.edtBuscar).text.toString().trim()
        container.removeAllViews()

        val filtrados = aparelhos.filter {
            it.nome.contains(filtro, ignoreCase = true) || it.tipo.contains(filtro, ignoreCase = true)
        }

        for (aparelho in filtrados) {
            val card = layoutInflater.inflate(R.layout.item_admin_aparelho, container, false)
            card.findViewById<TextView>(R.id.txtNomeAparelho).text = aparelho.nome
            card.findViewById<TextView>(R.id.txtTipoAparelho).text = aparelho.tipo

            val txtStatus = card.findViewById<TextView>(R.id.txtStatus)
            txtStatus.text = aparelho.status
            if (aparelho.status == "Manutenção") {
                txtStatus.setBackgroundResource(R.drawable.bg_badge_manutencao)
                txtStatus.setTextColor(ContextCompat.getColor(this, R.color.fm_yellow))
            }

            txtStatus.setOnClickListener { mostrarPopupStatus(aparelho) }
            container.addView(card)
        }
    }


    private fun mostrarPopupAdicionar() {
        val dialog = Dialogos.criar(this, R.layout.dialog_adicionar_aparelho)

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<View>(R.id.btnAdicionarAparelho).setOnClickListener {
            val edtNome = dialog.findViewById<EditText>(R.id.edtNomeAparelho)
            val nome = edtNome.text.toString().trim()
            if (nome.isEmpty()) {
                edtNome.error = "Preencha este campo"
                return@setOnClickListener
            }

            val tipo = dialog.findViewById<Spinner>(R.id.spnTipo).selectedItem.toString()
            val status = dialog.findViewById<Spinner>(R.id.spnStatus).selectedItem.toString()
            aparelhos.add(Aparelho(nome, tipo, status))
            renderizarLista()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun mostrarPopupStatus(aparelho: Aparelho) {
        val dialog = Dialogos.criar(this, R.layout.dialog_status_aparelho)

        dialog.findViewById<TextView>(R.id.txtTituloStatus).text = "Alterar status - ${aparelho.nome}"
        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        val spnStatus = dialog.findViewById<Spinner>(R.id.spnStatus)
        val opcoes = resources.getStringArray(R.array.status_aparelho)
        spnStatus.setSelection(opcoes.indexOf(aparelho.status).coerceAtLeast(0))

        dialog.findViewById<View>(R.id.btnSalvarStatus).setOnClickListener {
            aparelho.status = spnStatus.selectedItem.toString()
            renderizarLista()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerAparelhos)
        val bottomNav = findViewById<View>(R.id.bottomNavAdmin)
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
