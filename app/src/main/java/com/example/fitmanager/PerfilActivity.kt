package com.example.fitmanager

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import com.google.firebase.firestore.ListenerRegistration

class PerfilActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var uid = ""

    // Dados atuais, usados para preencher o popup "Editar Perfil"
    private var nomeAtual = ""
    private var pesoAtual: Double? = null
    private var alturaAtual: Double? = null
    private var fotoBase64: String? = null
    private var imgFotoDialog: ImageView? = null

    // Ouvintes do Firestore: ligados no onStart e desligados no onStop
    private var ouvintePerfil: ListenerRegistration? = null

    private var ouvinteObjetivos: ListenerRegistration? = null

    // Abre a galeria (Photo Picker do Android, sem precisar de permissão)
    private val escolherFoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) salvarFoto(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sem usuário logado, volta para o Login
        val usuario = auth.currentUser
        if (usuario == null) {
            irParaLogin()
            return
        }
        uid = usuario.uid
        DadosPadrao.garantir(uid)

        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ajustarBarras()

        findViewById<View>(R.id.btnConfiguracoes).setOnClickListener {
            startActivity(Intent(this, ConfiguracoesActivity::class.java))
        }
        findViewById<View>(R.id.btnAdicionarObjetivo).setOnClickListener {
            mostrarPopupAdicionarObjetivo()
        }
        findViewById<View>(R.id.btnEditarPerfil).setOnClickListener {
            mostrarPopupEditarPerfil()
        }

        BarraNavegacao.configurar(this, ItemNav.PERFIL)

        observarPerfil()
        observarObjetivos()
    }

    override fun onStart() {
        super.onStart()
        if (uid.isEmpty()) return
        observarPerfil()
        observarObjetivos()
    }

    override fun onStop() {
        super.onStop()
        ouvintePerfil?.remove()
        ouvinteObjetivos?.remove()
    }

    // Lê o documento usuarios/{uid} e atualiza a tela sempre que ele mudar
    private fun observarPerfil() {
        ouvintePerfil = db.collection("usuarios").document(uid)
            .addSnapshotListener { doc, erro ->
                if (erro != null || doc == null || !doc.exists()) return@addSnapshotListener

                nomeAtual = doc.getString("nome").orEmpty()
                val peso = doc.getDouble("peso")
                val altura = doc.getDouble("altura")
                val gc = doc.getDouble("gc")
                pesoAtual = peso
                alturaAtual = altura
                fotoBase64 = doc.getString("fotoBase64")

                val imc = if (peso != null && altura != null && altura > 0) {
                    peso / (altura * altura)
                } else null

                texto(R.id.txtNomeAluno, nomeAtual.ifBlank { "Aluno" })
                texto(R.id.txtPeso, peso?.let { "${formatarNumero(it)} kg" } ?: "-")
                texto(R.id.txtAltura, altura?.let { "${formatarNumero(it)} m" } ?: "-")
                texto(R.id.txtImc, imc?.let { String.format(LOCALE_BR, "%.1f", it) } ?: "-")
                texto(R.id.txtGc, gc?.let { "${formatarNumero(it)}%" } ?: "-")

                mostrarFoto(findViewById(R.id.imgAvatar), fotoBase64)
                mostrarFoto(imgFotoDialog, fotoBase64)
            }

    }

    // Lê a subcoleção usuarios/{uid}/objetivos e monta um item para cada um
    private fun observarObjetivos() {
        ouvinteObjetivos = db.collection("usuarios").document(uid).collection("objetivos")
            .addSnapshotListener { resultado, erro ->
                if (erro != null || resultado == null) return@addSnapshotListener

                val container = findViewById<LinearLayout>(R.id.listaObjetivos)
                container.removeAllViews()

                val docs = resultado.documents.sortedBy { it.getTimestamp("criadoEm")?.seconds ?: 0L }

                if (docs.isEmpty()) {
                    val vazio = TextView(this)
                    vazio.text = "Nenhum objetivo ainda. Toque no + para adicionar."
                    vazio.setTextColor(ContextCompat.getColor(this, R.color.fm_muted))
                    vazio.setPadding(0, 12, 0, 0)
                    container.addView(vazio)
                    return@addSnapshotListener
                }

                for (d in docs) {
                    val nome = d.getString("nome") ?: continue
                    val atual = d.getDouble("atual") ?: 0.0
                    val meta = d.getDouble("meta") ?: 0.0
                    val unidade = d.getString("unidade").orEmpty()

                    val item = layoutInflater.inflate(R.layout.item_objetivo, container, false)
                    item.findViewById<TextView>(R.id.txtObjetivo).text =
                        "$nome: ${formatarNumero(atual)} / ${formatarNumero(meta)} $unidade".trim()
                    item.findViewById<ProgressBar>(R.id.progressObjetivo).progress = progresso(atual, meta)
                    container.addView(item)
                }
            }
    }

    // Progresso = menor valor dividido pelo maior (serve para metas de subir ou de baixar)
    private fun progresso(atual: Double, meta: Double): Int {
        val maior = maxOf(atual, meta)
        if (maior <= 0.0) return 0
        return (minOf(atual, meta) / maior * 100).toInt()
    }

    // Popup "Adicionar Objetivo": grava um novo documento em usuarios/{uid}/objetivos
    private fun mostrarPopupAdicionarObjetivo() {
        val dialog = criarDialog(R.layout.dialog_adicionar_objetivo)
        val edtNome = dialog.findViewById<EditText>(R.id.edtNomeObjetivo)
        val edtAtual = dialog.findViewById<EditText>(R.id.edtValorAtual)
        val edtMeta = dialog.findViewById<EditText>(R.id.edtMeta)

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<View>(R.id.btnConfirmarObjetivo).setOnClickListener {
            val nome = edtNome.text.toString().trim()
            val atual = lerValor(edtAtual.text.toString())
            val meta = lerValor(edtMeta.text.toString())

            if (nome.isEmpty()) {
                edtNome.error = "Preencha este campo"
                return@setOnClickListener
            }
            if (atual == null) {
                edtAtual.error = "Digite um valor, ex.: 40kg"
                return@setOnClickListener
            }
            if (meta == null) {
                edtMeta.error = "Digite um valor, ex.: 60kg"
                return@setOnClickListener
            }

            val objetivo = hashMapOf(
                "nome" to nome,
                "atual" to atual.first,
                "meta" to meta.first,
                "unidade" to meta.second.ifEmpty { atual.second },
                "criadoEm" to FieldValue.serverTimestamp()
            )

            db.collection("usuarios").document(uid).collection("objetivos").add(objetivo)
                .addOnSuccessListener { dialog.dismiss() }
                .addOnFailureListener { e ->
                    Log.e("FIREBASE", "Erro ao salvar o objetivo", e)
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        dialog.show()
    }

    // Separa "40kg" em (40.0, "kg"); devolve null se não começar com número
    private fun lerValor(texto: String): Pair<Double, String>? {
        val resultado = Regex("""^\s*(\d+(?:[.,]\d+)?)\s*(.*)$""").find(texto) ?: return null
        val numero = resultado.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        return Pair(numero, resultado.groupValues[2].trim())
    }

    // Popup "Editar Perfil": atualiza nome, peso e altura em usuarios/{uid} e permite trocar a foto
    private fun mostrarPopupEditarPerfil() {
        val dialog = criarDialog(R.layout.dialog_editar_perfil)
        val edtNome = dialog.findViewById<EditText>(R.id.edtNome)
        val edtPeso = dialog.findViewById<EditText>(R.id.edtPeso)
        val edtAltura = dialog.findViewById<EditText>(R.id.edtAltura)

        edtNome.setText(nomeAtual)
        edtPeso.setText(pesoAtual?.let { formatarNumero(it) } ?: "")
        edtAltura.setText(alturaAtual?.let { formatarNumero(it) } ?: "")

        imgFotoDialog = dialog.findViewById(R.id.imgFotoDialog)
        mostrarFoto(imgFotoDialog, fotoBase64)
        dialog.setOnDismissListener { imgFotoDialog = null }

        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnAlterarFoto).setOnClickListener {
            escolherFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        dialog.findViewById<View>(R.id.btnSalvar).setOnClickListener {
            val nome = edtNome.text.toString().trim()
            val peso = edtPeso.text.toString().replace(',', '.').toDoubleOrNull()
            val altura = edtAltura.text.toString().replace(',', '.').toDoubleOrNull()

            if (nome.isEmpty()) {
                edtNome.error = "Preencha este campo"
                return@setOnClickListener
            }
            if (peso == null || peso <= 0) {
                edtPeso.error = "Digite um peso válido"
                return@setOnClickListener
            }
            if (altura == null || altura < 0.5 || altura > 2.5) {
                edtAltura.error = "Digite a altura em metros, ex.: 1,65"
                return@setOnClickListener
            }

            db.collection("usuarios").document(uid)
                .update(mapOf("nome" to nome, "peso" to peso, "altura" to altura))
                .addOnSuccessListener { dialog.dismiss() }
                .addOnFailureListener { e ->
                    Log.e("FIREBASE", "Erro ao salvar o perfil", e)
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        dialog.show()
    }

    // Reduz a foto escolhida, converte em texto (Base64) e grava em usuarios/{uid}
    private fun salvarFoto(uri: Uri) {
        val bitmap = carregarBitmapQuadrado(uri, 256)
        if (bitmap == null) {
            Toast.makeText(this, "Não foi possível ler a imagem", Toast.LENGTH_SHORT).show()
            return
        }

        val saida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saida)
        val base64 = Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP)

        db.collection("usuarios").document(uid)
            .update("fotoBase64", base64)
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "Erro ao salvar a foto", e)
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Abre a imagem já reduzida (evita estourar a memória) e recorta o centro em quadrado
    private fun carregarBitmapQuadrado(uri: Uri, lado: Int): Bitmap? {
        return try {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

            var amostra = 1
            while (limites.outWidth / (amostra * 2) >= lado && limites.outHeight / (amostra * 2) >= lado) {
                amostra *= 2
            }

            val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
            val original = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opcoes)
            } ?: return null

            val menor = minOf(original.width, original.height)
            val recorte = Bitmap.createBitmap(
                original, (original.width - menor) / 2, (original.height - menor) / 2, menor, menor
            )
            Bitmap.createScaledBitmap(recorte, lado, lado, true)
        } catch (e: Exception) {
            null
        }
    }

    // Converte o texto Base64 de volta em imagem e mostra na tela
    private fun mostrarFoto(imagem: ImageView?, base64: String?) {
        if (imagem == null) return
        if (base64.isNullOrEmpty()) {
            imagem.setImageDrawable(null)
            return
        }
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        imagem.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
    }

    private fun texto(id: Int, valor: String) {
        findViewById<TextView>(id).text = valor
    }

    private fun irParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Cria um popup com fundo transparente (para mostrar os cantos arredondados) e 90% da largura
    private fun criarDialog(layout: Int): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerPerfil)
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