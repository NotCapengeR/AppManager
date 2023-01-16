package ru.netology.app_manager.utils

fun Throwable.getErrorMessage(): String = this.message ?: this.toString()