package com.example.fitmanager

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration


// Dados do contrato já formatados para tela e PDF
private data class DadosContrato(
    val plano: String,
    val aluno: String,
    val matricula: String,
    val inicio: String,
    val vigencia: String,
    val valor: String,
    val status: String
)

class MeuContratoActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var contrato: DadosContrato? = null

    private var ouvinte: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meu_contrato)
        ajustarBarraDeStatus()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        findViewById<View>(R.id.btnBaixarPdf).setOnClickListener { baixarPdf() }
    }

    override fun onStart() {
        super.onStart()
        carregarContrato()
    }

    override fun onStop() {
        super.onStop()
        ouvinte?.remove()
    }

    // Busca o documento do aluno logado e preenche a tela
    private fun carregarContrato() {
        val uid = auth.currentUser?.uid ?: return

        ouvinte = db.collection("usuarios").document(uid)
            .addSnapshotListener { doc, erro ->
                if (erro != null) {
                    Log.e("FIREBASE", "Erro ao ler o contrato", erro)
                    Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (doc == null || !doc.exists()) return@addSnapshotListener

                val dados = DadosContrato(
                    plano = doc.getString("contrato.plano") ?: "Sem plano contratado",
                    aluno = doc.getString("nome") ?: "-",
                    matricula = doc.getString("matricula") ?: "-",
                    inicio = doc.getString("contrato.inicio") ?: "-",
                    vigencia = doc.getString("contrato.vigencia") ?: "-",
                    valor = doc.getDouble("contrato.valorMensal")?.let { formatarMoeda(it) } ?: "-",
                    status = doc.getString("contrato.status") ?: "-"
                )
                contrato = dados

                texto(R.id.txtPlano, dados.plano)
                texto(R.id.txtAluno, dados.aluno)
                texto(R.id.txtMatricula, dados.matricula)
                texto(R.id.txtInicio, dados.inicio)
                texto(R.id.txtVigencia, dados.vigencia)
                texto(R.id.txtValor, dados.valor)

                val ativo = dados.status.equals("Ativo", ignoreCase = true)
                val etiqueta = findViewById<TextView>(R.id.txtStatus)
                etiqueta.text = dados.status
                etiqueta.setTextColor(ContextCompat.getColor(this, if (ativo) R.color.fm_green else R.color.fm_red))
                etiqueta.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(if (ativo) "#DCFCE7" else "#FEE2E2"))
            }
    }

    // Botão "Baixar PDF": gera o arquivo com os dados do contrato e salva no aparelho
    private fun baixarPdf() {
        Log.d("PDF", "Botão tocado. Contrato carregado: ${contrato != null}")
        val dados = contrato
        if (dados == null) {
            Toast.makeText(this, "Aguarde o contrato carregar", Toast.LENGTH_SHORT).show()
            return
        }
        if (dados.plano == "Sem plano contratado") {
            Toast.makeText(this, "Você não possui um contrato ativo", Toast.LENGTH_SHORT).show()
            return
        }
        salvarPdf(montarPdf(dados), dados)
    }

    // Desenha a página do contrato (A4)
    private fun montarPdf(dados: DadosContrato): PdfDocument {
        val pdf = PdfDocument()
        val pagina = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = pagina.canvas

        val faixa = Paint().apply { color = Color.parseColor("#2563EB") }
        val titulo = Paint().apply {
            color = Color.WHITE; textSize = 22f; isFakeBoldText = true; isAntiAlias = true
        }
        val plano = Paint().apply {
            color = Color.parseColor("#111827"); textSize = 20f; isFakeBoldText = true; isAntiAlias = true
        }
        val rotulo = Paint().apply {
            color = Color.parseColor("#6B7280"); textSize = 14f; isAntiAlias = true
        }
        val valor = Paint().apply {
            color = Color.parseColor("#111827"); textSize = 14f; isFakeBoldText = true; isAntiAlias = true
        }

        canvas.drawRect(0f, 0f, 595f, 90f, faixa)
        canvas.drawText("FitManager - Academia Unifor", 40f, 55f, titulo)
        canvas.drawText(dados.plano, 40f, 140f, plano)

        val linhas = listOf(
            "Aluno" to dados.aluno,
            "Matrícula" to dados.matricula,
            "Início do contrato" to dados.inicio,
            "Vigência" to dados.vigencia,
            "Valor mensal" to dados.valor,
            "Status" to dados.status
        )
        var y = 190f
        for ((nome, conteudo) in linhas) {
            canvas.drawText(nome, 40f, y, rotulo)
            canvas.drawText(conteudo, 240f, y, valor)
            y += 36f
        }

        val emissao = SimpleDateFormat("dd/MM/yyyy HH:mm", LOCALE_BR).format(Date())
        canvas.drawText("Emitido em $emissao", 40f, 800f, rotulo)

        pdf.finishPage(pagina)
        return pdf
    }

    // Salva em Downloads (Android 10+) ou na pasta de documentos do app (versões antigas)
    private fun salvarPdf(pdf: PdfDocument, dados: DadosContrato) {
        val carimbo = SimpleDateFormat("yyyyMMdd_HHmmss", LOCALE_BR).format(Date())
        val nomeArquivo = "contrato_fitmanager_${dados.matricula}_$carimbo.pdf"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val valores = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nomeArquivo)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valores)
                if (uri == null) {
                    Toast.makeText(this, "Não foi possível criar o arquivo", Toast.LENGTH_SHORT).show()
                    return
                }
                contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }
                mostrarPdfSalvo(uri)
            } else {
                val arquivo = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), nomeArquivo)
                FileOutputStream(arquivo).use { pdf.writeTo(it) }
                Toast.makeText(this, "PDF salvo em ${arquivo.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("PDF", "Erro ao gerar o PDF", e)
            Toast.makeText(this, "Erro ao gerar o PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdf.close()
        }
    }

    private fun mostrarPdfSalvo(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("PDF salvo")
            .setMessage("O contrato foi salvo na pasta Downloads.")
            .setNegativeButton("Fechar", null)
            .setPositiveButton("Abrir") { _, _ -> abrirPdf(uri) }
            .show()
    }

    private fun abrirPdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Nenhum app para abrir PDF. O arquivo está em Downloads.", Toast.LENGTH_LONG).show()
        }
    }

    private fun texto(id: Int, valor: String) {
        findViewById<TextView>(id).text = valor
    }

    private fun ajustarBarraDeStatus() {
        val header = findViewById<View>(R.id.headerContrato)
        val paddingTopOriginal = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, 0)
            header.updatePadding(top = paddingTopOriginal + bars.top)
            insets
        }
    }
}