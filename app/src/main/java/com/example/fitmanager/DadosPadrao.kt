package com.example.fitmanager

import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

object DadosPadrao {

    // Preenche o que estiver faltando no documento do usuário (contas novas e antigas)
    fun garantir(uid: String) {
        val ref = FirebaseFirestore.getInstance().collection("usuarios").document(uid)

        ref.get().addOnSuccessListener { doc ->
            if (!doc.exists()) return@addOnSuccessListener

            val faltando = mutableMapOf<String, Any>()

            if (doc.getString("matricula").isNullOrEmpty()) {
                // Matrícula de exemplo, sempre a mesma para o mesmo usuário
                faltando["matricula"] = "2026" + Math.floorMod(uid.hashCode(), 10000).toString().padStart(4, '0')
            }

            if (doc.get("contrato") == null) {
                faltando["contrato"] = mapOf(
                    "plano" to "Plano Mensal Unifor Gym",
                    "inicio" to SimpleDateFormat("dd/MM/yyyy", LOCALE_BR).format(Date()),
                    "vigencia" to "12 meses",
                    "valorMensal" to 89.9,
                    "status" to "Ativo"
                )
            }

            if (faltando.isNotEmpty()) ref.update(faltando)
        }
    }
}