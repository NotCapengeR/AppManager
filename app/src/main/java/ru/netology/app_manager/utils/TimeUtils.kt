package ru.netology.app_manager.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    private val ABS_FORMATTER = SimpleDateFormat("dd-MM-yyyy, hh:mm:ss")

    @JvmStatic
    fun secondsToMin(leftSec: Int): String {
        val min: Int = leftSec / 60
        val sec: Int = leftSec % 60
        var s = if (min >= 10) min.toString() else "0$min"
        s = "$s:"
        s += if (sec >= 10) sec.toString() else "0$sec"
        return s
    }

    fun formatMillisAbs(millis: Long): String {
        return ABS_FORMATTER.format(Date(millis))
    }
}