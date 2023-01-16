package ru.netology.app_manager.core.api.exceptions

abstract class BaseCustomException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)