package com.example.fitmanager

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

enum class ItemNav { HOME, TREINOS, AULAS, NOTIFICACOES, PERFIL }

object BarraNavegacao {


    fun configurar(activity: Activity, ativo: ItemNav) {
        val abas = mapOf(
            ItemNav.HOME to R.id.navHome,
            ItemNav.TREINOS to R.id.navTreinos,
            ItemNav.AULAS to R.id.navAulas,
            ItemNav.NOTIFICACOES to R.id.navNotificacoes,
            ItemNav.PERFIL to R.id.navPerfil
        )

        for ((item, id) in abas) {
            val aba = activity.findViewById<ViewGroup>(id)
            val icone = aba.getChildAt(0)
            val rotulo = aba.getChildAt(1) as TextView

            val ehAtiva = item == ativo
            val corIcone = ContextCompat.getColor(activity, if (ehAtiva) R.color.fm_blue else R.color.fm_border)
            val corTexto = ContextCompat.getColor(activity, if (ehAtiva) R.color.fm_blue else R.color.fm_muted)

            icone.backgroundTintList = ColorStateList.valueOf(corIcone)
            rotulo.setTextColor(corTexto)

            aba.setOnClickListener {
                if (!ehAtiva) navegar(activity, item)
            }
        }
    }


    private fun navegar(activity: Activity, destino: ItemNav) {
        val tela: Class<out Activity>? = when (destino) {
            ItemNav.NOTIFICACOES -> NotificacoesActivity::class.java
            ItemNav.PERFIL -> PerfilActivity::class.java
            ItemNav.HOME -> HomeActivity::class.java
            ItemNav.TREINOS -> TreinosActivity::class.java
            ItemNav.AULAS -> AulasActivity::class.java
        }

        if (tela == null) {
            Toast.makeText(activity, "Tela em desenvolvimento", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(activity, tela)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        activity.startActivity(intent)
    }
}