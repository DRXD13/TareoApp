package com.dangeloretis.tareoapp.util

fun Long.formatHoursMinutes(): String {
    val hours = this / 60
    val minutes = this % 60
    return "${hours} h ${String.format(java.util.Locale.US, "%02d", minutes)} min"
}
