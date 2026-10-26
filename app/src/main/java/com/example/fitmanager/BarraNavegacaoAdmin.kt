package com.example.fitmanager

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

enum class ItemNavAdmin { HOME, USUARIOS, AULAS, EXERCICIOS, APARELHOS }

// Barra inferior das telas do administrador (layout_bottom_nav_admin)
object BarraNavegacaoAdmin {

    fun configurar(activity: Activity, ativo: ItemNavAdmin) {
        val abas = mapOf(
            ItemNavAdmin.HOME to R.id.navAdminHome,
            ItemNavAdmin.USUARIOS to R.id.navAdminUsuarios,
            ItemNavAdmin.AULAS to R.id.navAdminAulas,
            ItemNavAdmin.EXERCICIOS to R.id.navAdminExercicios,
            ItemNavAdmin.APARELHOS to R.id.navAdminAparelhos
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


    private fun navegar(activity: Activity, destino: ItemNavAdmin) {
        val tela: Class<out Activity>? = when (destino) {
            ItemNavAdmin.HOME -> AdminHomeActivity::class.java
            ItemNavAdmin.USUARIOS -> AdminUsuariosActivity::class.java
            ItemNavAdmin.APARELHOS -> AdminAparelhosActivity::class.java
            ItemNavAdmin.AULAS -> null        // TODO: gestão de aulas (issue #22)
            ItemNavAdmin.EXERCICIOS -> null   // TODO: gestão de exercícios (issue #17)
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
