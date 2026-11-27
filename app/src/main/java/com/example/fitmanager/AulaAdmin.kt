package com.example.fitmanager

import com.google.firebase.firestore.DocumentSnapshot

// Uma aula guardada na coleção "aulas" do Firestore
data class AulaAdmin(
    val id: String,
    val nome: String,
    val instrutor: String,
    val dia: String,
    val equipamento: String,
    val inicio: String,
    val fim: String,
    val vagas: Int,
    val matriculados: Int
) {
    companion object {
        // Converte um documento do Firestore em AulaAdmin
        fun de(doc: DocumentSnapshot): AulaAdmin = AulaAdmin(
            id = doc.id,
            nome = doc.getString("nome").orEmpty(),
            instrutor = doc.getString("instrutor").orEmpty(),
            dia = doc.getString("dia").orEmpty(),
            equipamento = doc.getString("equipamento").orEmpty(),
            inicio = doc.getString("inicio") ?: "00:00",
            fim = doc.getString("fim") ?: "00:00",
            vagas = doc.getLong("vagas")?.toInt() ?: 0,
            matriculados = doc.getLong("matriculados")?.toInt() ?: 0
        )
    }
}