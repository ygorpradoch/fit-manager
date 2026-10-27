package com.example.fitmanager

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class `AdminAulaDetalheActivity` : AppCompatActivity() {

    companion object {
        const val EXTRA_AULA = "aula"
    }


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

        val aula = lerAula()
        if (aula == null) {
            finish()
            return
        }
        preencher(aula)

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.AULAS)
    }

    @Suppress("DEPRECATION")
    private fun lerAula(): AulaAdmin? = intent.getSerializableExtra(EXTRA_AULA) as? AulaAdmin


    private fun preencher(aula: AulaAdmin) {
        texto(R.id.txtTituloDetalhe, aula.nome)
        texto(R.id.txtInicialInstrutor, aula.instrutor.take(2).uppercase())
        texto(R.id.txtNomeInstrutor, aula.instrutor)
        texto(R.id.txtMatriculaInstrutor, "Matrícula ${matriculas[aula.instrutor] ?: "-"}")
        texto(R.id.txtDia, aula.dia)
        texto(R.id.txtHorario, "${aula.inicio} - ${aula.fim}")
        texto(R.id.txtAlunos, "${aula.matriculados}/${aula.vagas}")
        texto(R.id.txtDuracao, "${minutos(aula.fim) - minutos(aula.inicio)} min")
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


    private fun minutos(hora: String): Int {
        val partes = hora.split(":")
        val h = partes.getOrNull(0)?.toIntOrNull() ?: 0
        val m = partes.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }


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
        val formato = DateTimeFormatter.ofPattern("dd 'de' MMM", Locale.forLanguageTag("pt-BR"))
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