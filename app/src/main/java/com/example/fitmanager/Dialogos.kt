package com.example.fitmanager

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

enum class EstiloBotao { PRIMARIO, PERIGO, CONTORNO, DESABILITADO }

object Dialogos {

    // Mesmo padrão dos outros popups do app: fundo transparente e 90% da largura
    fun criar(activity: Activity, layout: Int): Dialog {
        val dialog = Dialog(activity)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }

    // Popup de confirmação com dois botões (ex.: Confirmar Inscrição, Desativar Usuário)
    fun confirmar(
        activity: Activity,
        titulo: String,
        mensagem: String,
        textoEsquerda: String,
        estiloEsquerda: EstiloBotao,
        textoDireita: String,
        estiloDireita: EstiloBotao,
        aoClicarEsquerda: () -> Unit = {},
        aoClicarDireita: () -> Unit
    ) {
        val dialog = criar(activity, R.layout.dialog_confirmar)

        dialog.findViewById<TextView>(R.id.txtTituloDialogo).text = titulo
        dialog.findViewById<TextView>(R.id.txtMensagemDialogo).text = mensagem

        val btnEsquerda = dialog.findViewById<MaterialButton>(R.id.btnDialogoEsquerda)
        val btnDireita = dialog.findViewById<MaterialButton>(R.id.btnDialogoDireita)
        btnEsquerda.text = textoEsquerda
        btnDireita.text = textoDireita
        aplicarEstilo(btnEsquerda, estiloEsquerda)
        aplicarEstilo(btnDireita, estiloDireita)

        btnEsquerda.setOnClickListener {
            dialog.dismiss()
            aoClicarEsquerda()
        }
        btnDireita.setOnClickListener {
            dialog.dismiss()
            aoClicarDireita()
        }

        dialog.show()
    }

    // Troca a aparência de um botão em tempo de execução (azul, vermelho, contorno ou cinza)
    fun aplicarEstilo(botao: MaterialButton, estilo: EstiloBotao) {
        val contexto = botao.context
        val azul = ContextCompat.getColor(contexto, R.color.fm_blue)
        val vermelho = ContextCompat.getColor(contexto, R.color.fm_red)
        val cinza = ContextCompat.getColor(contexto, R.color.fm_border)
        val branco = ContextCompat.getColor(contexto, android.R.color.white)

        botao.isEnabled = true
        botao.strokeWidth = 0

        when (estilo) {
            EstiloBotao.PRIMARIO -> {
                botao.backgroundTintList = ColorStateList.valueOf(azul)
                botao.setTextColor(branco)
            }
            EstiloBotao.PERIGO -> {
                botao.backgroundTintList = ColorStateList.valueOf(vermelho)
                botao.setTextColor(branco)
            }
            EstiloBotao.CONTORNO -> {
                botao.backgroundTintList = ColorStateList.valueOf(branco)
                botao.setTextColor(azul)
                botao.strokeColor = ColorStateList.valueOf(azul)
                botao.strokeWidth = (1.5f * botao.resources.displayMetrics.density).toInt()
            }
            EstiloBotao.DESABILITADO -> {
                botao.backgroundTintList = ColorStateList.valueOf(cinza)
                botao.setTextColor(branco)
                botao.isEnabled = false
            }
        }
    }
}
