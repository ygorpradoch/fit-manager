package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton

class AdminAparelhosActivity : AppCompatActivity() {

    // Aparelho de exemplo (sem banco por enquanto)
    private class Aparelho(
        val nome: String,
        val tipo: String,
        var operacional: Boolean = true
    )

    private val tipos = listOf("Cardio", "Musculação")
    private val statusPossiveis = listOf("Operacional", "Manutenção")

    private val aparelhos = mutableListOf(
        Aparelho("Esteira", "Cardio"),
        Aparelho("Leg Press", "Musculação"),
        Aparelho("Cadeira Extensora", "Musculação", operacional = false),
        Aparelho("Bicicleta", "Cardio")
    )

    private lateinit var edtBusca: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_aparelhos)
        ajustarBarras()

        edtBusca = findViewById(R.id.edtBusca)

        // A seta leva de volta para a Home do admin, limpando as telas que estiverem por cima dela
        findViewById<View>(R.id.btnVoltar).setOnClickListener {
            val intent = Intent(this, AdminHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        findViewById<View>(R.id.btnAdicionar).setOnClickListener { mostrarPopupAdicionar() }

        edtBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = montarLista()
        })

        montarLista()
        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.APARELHOS)
    }

    // Filtra pelo texto da busca (nome ou tipo) e monta um card por aparelho
    private fun montarLista() {
        val container = findViewById<LinearLayout>(R.id.listaAparelhos)
        container.removeAllViews()

        val termo = edtBusca.text.toString().trim()
        val filtrados = aparelhos.filter {
            it.nome.contains(termo, ignoreCase = true) || it.tipo.contains(termo, ignoreCase = true)
        }

        if (filtrados.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = "Nenhum aparelho encontrado."
            vazio.setTextColor(getColor(R.color.fm_muted))
            vazio.textSize = 14f
            vazio.setPadding(0, 24, 0, 0)
            container.addView(vazio)
            return
        }

        for (aparelho in filtrados) {
            val card = layoutInflater.inflate(R.layout.item_aparelho, container, false)
            card.findViewById<TextView>(R.id.txtNomeAparelho).text = aparelho.nome
            card.findViewById<TextView>(R.id.txtTipoAparelho).text = aparelho.tipo

            val selo = card.findViewById<TextView>(R.id.txtStatusAparelho)
            if (aparelho.operacional) {
                selo.text = "Operacional"
                selo.setBackgroundResource(R.drawable.bg_badge_ativo)
                selo.setTextColor(getColor(R.color.fm_green))
            } else {
                selo.text = "Manutenção"
                selo.setBackgroundResource(R.drawable.bg_badge_manutencao)
                selo.setTextColor(getColor(R.color.fm_warning))
            }
            selo.setOnClickListener { mostrarPopupStatus(aparelho) }

            container.addView(card)
        }
    }

    private fun mostrarPopupAdicionar() {
        val dialog = Dialogos.criar(this, R.layout.dialog_aparelho)

        dialog.findViewById<TextView>(R.id.txtTituloDialog).text = "Adicionar novo aparelho"
        dialog.findViewById<MaterialButton>(R.id.btnConfirmar).text = "Adicionar"

        val edtNome = dialog.findViewById<EditText>(R.id.edtNomeAparelho)
        val spnTipo = dialog.findViewById<Spinner>(R.id.spnTipo)
        val spnStatus = dialog.findViewById<Spinner>(R.id.spnStatus)
        val txtErro = dialog.findViewById<TextView>(R.id.txtErroDialog)

        spnTipo.adapter = adaptador(tipos)
        spnStatus.adapter = adaptador(statusPossiveis)

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnConfirmar).setOnClickListener {
            val nome = edtNome.text.toString().trim()
            if (nome.isEmpty()) {
                txtErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // TODO: salvar no backend
            aparelhos.add(
                Aparelho(nome, tipos[spnTipo.selectedItemPosition], spnStatus.selectedItemPosition == 0)
            )
            dialog.dismiss()
            montarLista()
        }

        dialog.show()
    }

    private fun mostrarPopupStatus(aparelho: Aparelho) {
        val dialog = Dialogos.criar(this, R.layout.dialog_aparelho)

        dialog.findViewById<TextView>(R.id.txtTituloDialog).text = "Alterar status - ${aparelho.nome}"
        dialog.findViewById<MaterialButton>(R.id.btnConfirmar).text = "Salvar"

        // Alterar status só mostra o campo Status
        dialog.findViewById<View>(R.id.grupoNomeTipo).visibility = View.GONE

        val spnStatus = dialog.findViewById<Spinner>(R.id.spnStatus)
        spnStatus.adapter = adaptador(statusPossiveis)
        spnStatus.setSelection(if (aparelho.operacional) 0 else 1)

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnConfirmar).setOnClickListener {
            // TODO: salvar no backend
            aparelho.operacional = spnStatus.selectedItemPosition == 0
            dialog.dismiss()
            montarLista()
        }

        dialog.show()
    }

    private fun adaptador(opcoes: List<String>) =
        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcoes)

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerAparelhos)
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
