package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton


data class ExercicioAdmin(
    val nome: String,
    val categorias: List<String>,
    val aparelho: String,
    val dificuldade: String,
    val instrucoes: String,
    val video: String
) : java.io.Serializable

class AdminExerciciosActivity : AppCompatActivity() {

    private val exercicios = mutableListOf(
        ExercicioAdmin(
            "Supino Reto", listOf("Peito", "Braços"), "Banco de Supino", "Intermediário",
            "Deite no banco com os pés apoiados no chão, segure a barra com as mãos um pouco mais afastadas que a largura dos ombros e desça controladamente até tocar o peito, empurrando de volta em seguida.",
            "https://www.youtube.com/results?search_query=supino+reto"
        ),
        ExercicioAdmin(
            "Agachamento", listOf("Pernas"), "Barra", "Iniciante",
            "Fique com os pés na largura dos ombros, desça o quadril como se fosse sentar mantendo as costas retas e volte a ficar em pé.",
            ""
        ),
        ExercicioAdmin(
            "Levantamento Terra", listOf("Costas", "Pernas"), "Barra", "Avançado",
            "Com a barra no chão, segure com as mãos na largura dos ombros e levante mantendo a coluna reta até ficar em pé.",
            ""
        ),
        ExercicioAdmin(
            "Rosca Direta", listOf("Braços"), "Halteres", "Iniciante",
            "Em pé, segure os halteres com as palmas para frente e dobre os cotovelos levando o peso até a altura dos ombros.",
            ""
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_exercicios)
        ajustarBarras()

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }

        findViewById<View>(R.id.btnAdicionar).setOnClickListener {
            startActivity(Intent(this, AdminExercicioFormActivity::class.java))
        }

        findViewById<EditText>(R.id.edtBuscar).doAfterTextChanged { renderizarLista() }

        BarraNavegacaoAdmin.configurar(this, ItemNavAdmin.EXERCICIOS)
        renderizarLista()
    }


    private fun renderizarLista() {
        val container = findViewById<LinearLayout>(R.id.listaExercicios)
        val filtro = findViewById<EditText>(R.id.edtBuscar).text.toString().trim()
        container.removeAllViews()

        for (exercicio in exercicios.filter { it.nome.contains(filtro, ignoreCase = true) }) {
            val card = layoutInflater.inflate(R.layout.item_admin_exercicio, container, false)
            card.findViewById<TextView>(R.id.txtNomeExercicio).text = exercicio.nome
            card.findViewById<TextView>(R.id.txtCategoria).text = exercicio.categorias.firstOrNull() ?: ""
            card.findViewById<View>(R.id.btnMaisExercicio).setOnClickListener { mostrarMenu(exercicio) }
            container.addView(card)
        }
    }


    // usa o mesmo popup de menu das aulas, so troca os textos dos botoes
    private fun mostrarMenu(exercicio: ExercicioAdmin) {
        val dialog = Dialogos.criar(this, R.layout.dialog_admin_aula_menu)

        dialog.findViewById<TextView>(R.id.txtTituloMenu).text = exercicio.nome
        dialog.findViewById<MaterialButton>(R.id.btnEditar).text = "Editar Exercício"
        dialog.findViewById<MaterialButton>(R.id.btnExcluir).text = "Excluir Exercício"
        dialog.findViewById<View>(R.id.btnFechar).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<View>(R.id.btnVerDetalhes).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, AdminExercicioDetalheActivity::class.java)
            intent.putExtra(AdminExercicioDetalheActivity.EXTRA_EXERCICIO, exercicio)
            startActivity(intent)
        }

        dialog.findViewById<View>(R.id.btnEditar).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, AdminExercicioFormActivity::class.java)
            intent.putExtra(AdminExercicioFormActivity.EXTRA_EXERCICIO, exercicio)
            startActivity(intent)
        }

        dialog.findViewById<View>(R.id.btnExcluir).setOnClickListener {
            dialog.dismiss()
            Dialogos.confirmar(
                this,
                titulo = "Excluir Exercício?",
                mensagem = "Essa ação remove o exercício de todos os treinos que o utilizam.",
                textoEsquerda = "Cancelar",
                estiloEsquerda = EstiloBotao.CONTORNO,
                textoDireita = "Confirmar",
                estiloDireita = EstiloBotao.PERIGO
            ) {
                exercicios.remove(exercicio)
                renderizarLista()
            }
        }

        dialog.show()
    }

    private fun ajustarBarras() {
        val header = findViewById<View>(R.id.headerExercicios)
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
