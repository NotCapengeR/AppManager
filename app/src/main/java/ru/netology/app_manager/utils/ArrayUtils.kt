package ru.netology.app_manager.utils

fun <T> List<T>.combine(other: List<T>): List<T> = this + other
fun <T> List<T>.combineNotNull(other: List<T>?): List<T> {
    if (other == null) return this
    return this + other
}