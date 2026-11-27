package com.example.fitmanager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class AdminAulaDetalheActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AULA_ID = "aulaId"
    }

    private val db = FirebaseFirestore.getInstance()
    private var ouvinte: ListenerRegistration? = null
    private var aulaId = ""

    // Matrículas de exemplo dos instrutores (virão de um cadastro de instrutores depois)
    private val matriculas = mapOf(
        "Ronnison" to "205304",
        "Marcelo" to "205311",
        "Camila" to "205327"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_aula_detalhe)
        ajustarBarras()

        aulaId = intent.getStringExtra(EXTRA_AULA_ID).orEmpty()
        if (aulaId.isEmpty()) {
            finish()
            return
        }

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.AULAS)
    }

    override fun onStart() {
        super.onStart()
        if (aulaId.isEmpty()) return
        observarAula()
    }

    override fun onStop() {
        super.onStop()
        ouvinte?.remove()
    }

    // Lê o documento aulas/{id} e atualiza a tela sempre que ele mudar
    private fun observarAula() {
        ouvinte = db.collection("aulas").document(aulaId)
            .addSnapshotListener { doc, erro ->
                if (erro != null) {
                    Log.e("FIREBASE", "Erro ao ler a aula", erro)
                    Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (doc == null) return@addSnapshotListener
                if (!doc.exists()) {
                    // A aula foi excluída enquanto a tela estava aberta
                    finish()
                    return@addSnapshotListener
                }
                preencher(AulaAdmin.de(doc))
            }
    }

    // Distribui os dados da aula nos campos da tela
    private fun preencher(aula: AulaAdmin) {
        val duracao = horaParaMinutos(aula.fim) - horaParaMinutos(aula.inicio)

        texto(R.id.txtTituloDetalhe, aula.nome)
        texto(R.id.txtInicialInstrutor, aula.instrutor.take(2).uppercase())
        texto(R.id.txtNomeInstrutor, aula.instrutor)
        texto(R.id.txtMatriculaInstrutor, "Matrícula ${matriculas[aula.instrutor] ?: "-"}")
        texto(R.id.txtDia, aula.dia)
        texto(R.id.txtHorario, "${aula.inicio} - ${aula.fim}")
        texto(R.id.txtAlunos, "${aula.matriculados}/${aula.vagas}")
        texto(R.id.txtDuracao, "$duracao min")
        texto(R.id.txtEquipamentos, aula.equipamento)

        val datas = proximasDatas(aula.dia)
        val idsData = listOf(R.id.txtData1, R.id.txtData2, R.id.txtData3)
        val idsHora = listOf(R.id.txtHora1, R.id.txtHora2, R.id.txtHora3)
        for (i in 0..2) {
            texto(idsData[i], datas[i])
            texto(idsHora[i], aula.inicio)
        }
    }

    private fun texto(id: Int, valor: String) {
        findViewById<TextView>(id).text = valor
    }

    // Calcula as próximas três datas em que a aula acontece
    private fun proximasDatas(dia: String): List<String> {
        val diaSemana = when (dia) {
            "Segunda" -> DayOfWeek.MONDAY
            "Terça" -> DayOfWeek.TUESDAY
            "Quarta" -> DayOfWeek.WEDNESDAY
            "Quinta" -> DayOfWeek.THURSDAY
            "Sexta" -> DayOfWeek.FRIDAY
            "Sábado" -> DayOfWeek.SATURDAY
            else -> DayOfWeek.SUNDAY
        }
        val formato = DateTimeFormatter.ofPattern("dd 'de' MMM", LOCALE_BR)
        val primeira = LocalDate.now().with(TemporalAdjusters.nextOrSame(diaSemana))
        return (0..2).map { primeira.plusWeeks(it.toLong()).format(formato) }
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerDetalhe)
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