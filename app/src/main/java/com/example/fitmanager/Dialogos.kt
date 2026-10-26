package com.example.fitmanager

import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

// Montagem dos popups arredondados usados nas telas (mesmo visual do Perfil)
object Dialogos {

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

    // Popup de confirmação com dois botões. O da esquerda só fecha; o da direita executa a ação.
    fun confirmar(
        activity: Activity,
        titulo: String,
        mensagem: String,
        textoEsquerda: String,
        textoDireita: String,
        corDireita: Int = R.color.fm_blue,
        esquerdaPreenchida: Boolean = false,
        aoConfirmar: () -> Unit
    ) {
        val dialog = criar(activity, R.layout.dialog_confirmar)

        dialog.findViewById<TextView>(R.id.txtTituloDialog).text = titulo
        dialog.findViewById<TextView>(R.id.txtMensagemDialog).text = mensagem

        val btnEsquerda = dialog.findViewById<MaterialButton>(R.id.btnEsquerda)
        val btnDireita = dialog.findViewById<MaterialButton>(R.id.btnDireita)
        btnEsquerda.text = textoEsquerda
        btnDireita.text = textoDireita

        estilizar(activity, btnEsquerda, R.color.fm_blue, esquerdaPreenchida)
        estilizar(activity, btnDireita, corDireita, true)

        btnEsquerda.setOnClickListener { dialog.dismiss() }
        btnDireita.setOnClickListener {
            dialog.dismiss()
            aoConfirmar()
        }

        dialog.show()
    }

    // Botão cheio (fundo colorido) ou vazado (só borda colorida)
    fun estilizar(activity: Activity, botao: MaterialButton, cor: Int, preenchido: Boolean) {
        val corResolvida = ContextCompat.getColor(activity, cor)
        if (preenchido) {
            botao.backgroundTintList = ColorStateList.valueOf(corResolvida)
            botao.setTextColor(ContextCompat.getColor(activity, android.R.color.white))
            botao.strokeWidth = 0
        } else {
            botao.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(activity, android.R.color.white))
            botao.setTextColor(corResolvida)
            botao.strokeColor = ColorStateList.valueOf(corResolvida)
            botao.strokeWidth = (1.5f * activity.resources.displayMetrics.density).toInt()
        }
    }
}
