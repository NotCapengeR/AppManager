package ru.netology.app_manager.di.modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import ru.netology.app_manager.core.api.repository.AuthManager
import ru.netology.app_manager.core.api.service.BackendService
import ru.netology.app_manager.core.api.service.VirusTotalService
import timber.log.Timber
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.SocketFactory

@Module
class NetworkModule {

    private companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideConverterFactory(): Converter.Factory =
        json.asConverterFactory("application/json".toMediaType())


    @Provides
    @Singleton
    fun provideVirusTotalService(
        factory: Converter.Factory,
        client: OkHttpClient
    ): VirusTotalService = Retrofit.Builder()
        .baseUrl(VirusTotalService.BASE_URL)
        .addConverterFactory(factory)
        .client(client)
        .build()
        .create(VirusTotalService::class.java)


    @Provides
    @Singleton
    fun provideBackendService(
        factory: Converter.Factory,
        auth: AuthManager,
        builder: OkHttpClient.Builder
    ): BackendService = Retrofit.Builder()
        .baseUrl(BackendService.BASE_URL)
        .addConverterFactory(factory)
        .client(
            builder.addInterceptor { chain ->
                auth.token.value?.let { token ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", token)
                        .build()
                    return@addInterceptor chain.proceed(request)
                }
                chain.proceed(chain.request())
            }.build()
        )
        .build()
        .create(BackendService::class.java)

    @Provides
    @Singleton
    fun provideBaseClient(builder: OkHttpClient.Builder): OkHttpClient = builder.build()

    @Provides
    @Singleton
    fun provideHttpClientBuilder(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .protocols(listOf(Protocol.HTTP_1_1, Protocol.HTTP_2))
        .retryOnConnectionFailure(true)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Connection", "close")
                .build()
            return@addInterceptor chain.proceed(request)
        }


    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            Timber.tag("Retrofit").d(message)
        }.setLevel(HttpLoggingInterceptor.Level.BODY)
}