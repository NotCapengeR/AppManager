package ru.netology.app_manager.utils

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong

object MathUtils {
    const val BYTE_LEN = 1024


}

fun Int.pow(exponent: Int): Double = this.toDouble().pow(exponent)
fun Int.pow(exponent: Double): Double = this.toDouble().pow(exponent)

fun Long.pow(exponent: Int): Double = this.toDouble().pow(exponent)
fun Long.pow(exponent: Double): Double = this.toDouble().pow(exponent)

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}