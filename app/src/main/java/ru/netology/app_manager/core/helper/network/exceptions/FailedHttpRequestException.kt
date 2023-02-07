package ru.netology.app_manager.core.helper.network.exceptions


import retrofit2.HttpException
import retrofit2.Response

class FailedHttpRequestException(val response: Response<*>) : HttpException(response)


class EmptyBodyException(response: Response<*>): HttpException(response)