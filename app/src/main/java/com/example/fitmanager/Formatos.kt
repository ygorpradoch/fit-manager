package com.example.fitmanager

import java.text.NumberFormat
import java.util.Locale

val LOCALE_BR: Locale = Locale.forLanguageTag("pt-BR")


fun formatarNumero(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString().replace('.', ',')


fun formatarMoeda(valor: Double): String =
    NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor)