package com.kushwahahardware.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val decimalFormat = DecimalFormat("#,##0.00")
    
    init {
        currencyFormat.maximumFractionDigits = 2
    }
    
    fun format(amount: Double): String {
        return currencyFormat.format(amount)
    }
    
    fun formatWithSymbol(amount: Double): String {
        return "₹${decimalFormat.format(amount)}"
    }
    
    fun formatWithoutSymbol(amount: Double): String {
        return decimalFormat.format(amount)
    }
    
    fun parse(amountString: String): Double {
        return try {
            amountString.replace("[₹,\\s]".toRegex(), "").toDouble()
        } catch (e: Exception) {
            0.0
        }
    }
}