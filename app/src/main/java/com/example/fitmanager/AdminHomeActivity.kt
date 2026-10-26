package com.example.fitmanager

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.progressindicator.LinearProgressIndicator

class AdminHomeActivity : AppCompatActivity() {

    // Dados de exemplo, sem banco por enquanto
    private val aulasDoDia = listOf(
        "Zumba" to "08:00 - 09:00",
        "Jiu Jitsu" to "13:00 - 15:00"
    )

    // Nome da aula e percentual de ocupação
    private val aulasPopulares = listOf(
        "Zumba" to 90,
        "Crossfit" to 80,
        "Yoga" to 65,
        "Spinning" to 55,
        "Boxe" to 40
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_home)
        ajustarBarras()

        montarAulasDoDia()
        montarIndicadores()
        montarPopulares()

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.HOME)
    }

    private fun montarAulasDoDia() {
        val container = findViewById<LinearLayout>(R.id.listaAulasDia)
        aulasDoDia.forEachIndexed { indice, (nome, horario) ->
            val linha = layoutInflater.inflate(R.layout.item_aula_dia_admin, container, false)
            linha.findViewById<TextView>(R.id.txtNomeAulaDia).text = nome
            linha.findViewById<TextView>(R.id.txtHorarioAulaDia).text = horario
            container.addView(linha)

            // Divisor entre as linhas, a última fica sem
            if (indice != aulasDoDia.lastIndex) {
                val divisor = layoutInflater.inflate(R.layout.item_divisor, container, false)
                container.addView(divisor)
            }
        }
    }

    private fun montarIndicadores() {
        preencherIndicador(R.id.indUsuarios, "Total de Usuários", "1.230", "+18% vs. mês passado")
        preencherIndicador(R.id.indAparelhos, "Aparelhos Cadastrados", "102", "+7% vs. mês passado")
        preencherIndicador(R.id.indAulas, "Total de Aulas", "36", "+7% vs. mês passado")
    }

    private fun preencherIndicador(id: Int, titulo: String, valor: String, variacao: String) {
        val card = findViewById<View>(id)
        card.findViewById<TextView>(R.id.txtIndicadorTitulo).text = titulo
        card.findViewById<TextView>(R.id.txtIndicadorValor).text = valor
        card.findViewById<TextView>(R.id.txtIndicadorVariacao).text = variacao
    }

    private fun montarPopulares() {
        val container = findViewById<LinearLayout>(R.id.listaPopulares)
        for ((nome, percentual) in aulasPopulares) {
            val item = layoutInflater.inflate(R.layout.item_aula_popular, container, false)
            item.findViewById<TextView>(R.id.txtNomePopular).text = nome
            item.findViewById<LinearProgressIndicator>(R.id.barraPopular).progress = percentual
            container.addView(item)
        }
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerAdminHome)
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
