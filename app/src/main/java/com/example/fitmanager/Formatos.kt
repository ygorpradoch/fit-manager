package com.example.fitmanager

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val LOCALE_BR: Locale = Locale.forLanguageTag("pt-BR")

// 62.0 vira "62" e 1.65 vira "1,65"
fun formatarNumero(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString().replace('.', ',')

// 89.9 vira "R$ 89,90"
fun formatarMoeda(valor: Double): String =
    NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor)

// Data da notificação vira "Há 2 horas", "Há 1 dia" etc.
fun formatarTempoDecorrido(data: Date?): String {
    if (data == null) return ""
    val minutos = (System.currentTimeMillis() - data.time) / 60_000
    return when {
        minutos < 1 -> "Agora mesmo"
        minutos < 60 -> "Há $minutos min"
        minutos < 24 * 60 -> {
            val horas = minutos / 60
            if (horas == 1L) "Há 1 hora" else "Há $horas horas"
        }
        minutos < 30 * 24 * 60 -> {
            val dias = minutos / (24 * 60)
            if (dias == 1L) "Há 1 dia" else "Há $dias dias"
        }
        else -> SimpleDateFormat("dd/MM/yyyy", LOCALE_BR).format(data)
    }
}