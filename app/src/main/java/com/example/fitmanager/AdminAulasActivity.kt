package com.example.fitmanager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged


data class AulaAdmin(
    val nome: String,
    val instrutor: String,
    val dia: String,
    val equipamento: String,
    val inicio: String,
    val fim: String,
    val vagas: Int,
    val matriculados: Int
) : java.io.Serializable

class AdminAulasActivity : AppCompatActivity() {

    private val aulas = mutableListOf(
        AulaAdmin("Yoga", "Ronnison", "Segunda", "Tapetes", "08:00", "09:00", 15, 12),
        AulaAdmin("Boxe", "Marcelo", "Quarta", "Cordas", "19:00", "20:00", 20, 16),
        AulaAdmin("Spinning", "Camila", "Sexta", "Bicicletas", "18:00", "19:00", 20, 18)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_aulas)
        ajustarBarras()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() } // TODO: voltar para a Home do admin

        findViewById<View>(R.id.btnAdicionar).setOnClickListener {
            startActivity(Intent(this, AdminAulaFormActivity::class.java))
        }


        findViewById<EditText>(R.id.edtBuscar).doAfterTextChanged { renderizarLista() }

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.AULAS)
        renderizarLista()
    }


    private fun renderizarLista() {
        val container = findViewById<LinearLayout>(R.id.listaAulas)
        val filtro = findViewById<EditText>(R.id.edtBuscar).text.toString().trim()
        container.removeAllViews()

        for (aula in aulas.filter { it.nome.contains(filtro, ignoreCase = true) }) {
            val card = layoutInflater.inflate(R.layout.item_admin_aula, container, false)
            card.findViewById<TextView>(R.id.txtNomeAula).text = aula.nome
            card.findViewById<TextView>(R.id.txtMatriculados).text =
                "${aula.matriculados}/${aula.vagas} matriculados"
            card.findViewById<View>(R.id.btnMaisAula).setOnClickListener { mostrarMenu(aula) }
            container.addView(card)
        }
    }


    private fun mostrarMenu(aula: AulaAdmin) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_admin_aula_menu)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<TextView>(R.id.txtTituloMenu).text = aula.nome
        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<View>(R.id.btnVerDetalhes).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, `AdminAulaDetalheActivity`::class.java)
            intent.putExtra(`AdminAulaDetalheActivity`.EXTRA_AULA, aula)
            startActivity(intent)
        }

        dialog.findViewById<View>(R.id.btnEditar).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, AdminAulaFormActivity::class.java)
            intent.putExtra(AdminAulaFormActivity.EXTRA_AULA, aula)
            startActivity(intent)
        }

        dialog.findViewById<View>(R.id.btnExcluir).setOnClickListener {
            dialog.dismiss()
            confirmarExclusao(aula)
        }

        dialog.show()
    }


    private fun confirmarExclusao(aula: AulaAdmin) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_admin_confirmar_exclusao)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<View>(R.id.btnCancelarExclusao).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnConfirmarExclusao).setOnClickListener {
            aulas.remove(aula)
            renderizarLista()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerAulas)
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