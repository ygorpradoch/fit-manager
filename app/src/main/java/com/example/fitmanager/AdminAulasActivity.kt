package com.example.fitmanager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminAulasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var aulas = listOf<AulaAdmin>()
    private var ouvinte: ListenerRegistration? = null
    private var carregou = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_aulas)
        ajustarBarras()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() } // TODO: voltar para a Home do admin

        findViewById<View>(R.id.btnAdicionar).setOnClickListener {
            startActivity(Intent(this, AdminAulaFormActivity::class.java))
        }

        // Filtra a lista enquanto o usuário digita
        findViewById<EditText>(R.id.edtBuscar).doAfterTextChanged { renderizarLista() }

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.AULAS)
    }

    // O ouvinte do Firestore liga quando a tela aparece e desliga quando ela sai
    override fun onStart() {
        super.onStart()
        observarAulas()
    }

    override fun onStop() {
        super.onStop()
        ouvinte?.remove()
    }

    // Lê a coleção "aulas" e atualiza a lista sempre que algo mudar no banco
    private fun observarAulas() {
        ouvinte = db.collection("aulas").addSnapshotListener { resultado, erro ->
            if (erro != null) {
                Log.e("FIREBASE", "Erro ao ler as aulas", erro)
                Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            if (resultado == null) return@addSnapshotListener

            aulas = resultado.documents.map { AulaAdmin.de(it) }.sortedBy { it.nome.lowercase() }
            carregou = true
            renderizarLista()
        }
    }

    // Monta um card por aula, respeitando o texto da busca
    private fun renderizarLista() {
        if (!carregou) return

        val container = findViewById<LinearLayout>(R.id.listaAulas)
        val filtro = findViewById<EditText>(R.id.edtBuscar).text.toString().trim()
        container.removeAllViews()

        val lista = aulas.filter { it.nome.contains(filtro, ignoreCase = true) }

        if (lista.isEmpty()) {
            val vazio = TextView(this)
            vazio.text = if (filtro.isEmpty()) {
                "Nenhuma aula cadastrada. Toque em + Adicionar."
            } else {
                "Nenhuma aula encontrada."
            }
            vazio.setTextColor(ContextCompat.getColor(this, R.color.fm_muted))
            vazio.setPadding(0, 24, 0, 0)
            container.addView(vazio)
            return
        }

        for (aula in lista) {
            val card = layoutInflater.inflate(R.layout.item_admin_aula, container, false)
            card.findViewById<TextView>(R.id.txtNomeAula).text = aula.nome
            card.findViewById<TextView>(R.id.txtMatriculados).text =
                "${aula.matriculados}/${aula.vagas} matriculados"
            card.findViewById<View>(R.id.btnMaisAula).setOnClickListener { mostrarMenu(aula) }
            container.addView(card)
        }
    }

    // Popup com as ações da aula: Ver Detalhes, Editar e Excluir
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
            val intent = Intent(this, AdminAulaDetalheActivity::class.java)
            intent.putExtra(AdminAulaDetalheActivity.EXTRA_AULA_ID, aula.id)
            startActivity(intent)
        }

        dialog.findViewById<View>(R.id.btnEditar).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, AdminAulaFormActivity::class.java)
            intent.putExtra(AdminAulaFormActivity.EXTRA_AULA_ID, aula.id)
            startActivity(intent)
        }

        dialog.findViewById<View>(R.id.btnExcluir).setOnClickListener {
            dialog.dismiss()
            confirmarExclusao(aula)
        }

        dialog.show()
    }

    // Confirmação antes de excluir a aula (RF16.4)
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
            excluirAula(aula)
            dialog.dismiss()
        }

        dialog.show()
    }

    // Apaga o documento da aula; a lista se atualiza sozinha pelo ouvinte
    private fun excluirAula(aula: AulaAdmin) {
        db.collection("aulas").document(aula.id).delete()
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "Erro ao excluir a aula", e)
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
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