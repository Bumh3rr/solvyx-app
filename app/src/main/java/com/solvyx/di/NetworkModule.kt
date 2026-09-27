package com.solvyx.di

import com.google.firebase.auth.FirebaseAuth
import com.solvyx.backend.data.remote.ApiConfig
import com.solvyx.backend.data.remote.ApiLoggingInterceptor
import com.solvyx.backend.data.remote.chat.ChatApi
import com.solvyx.backend.data.remote.chat.ChatCredentials
import com.solvyx.backend.data.remote.chat.ChatCredentialsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Sin reintentos silenciosos: un POST repetido duplicaría el mensaje en el historial del servidor.
            .retryOnConnectionFailure(false)
            .addInterceptor(ApiLoggingInterceptor())
            .build()

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

    @Provides
    fun provideChatCredentialsProvider(firebaseAuth: FirebaseAuth): ChatCredentialsProvider =
        ChatCredentialsProvider {
            val user = firebaseAuth.currentUser ?: return@ChatCredentialsProvider null
            // false: Firebase reuses the cached token and only refreshes it when it is about to expire.
            val token = checkNotNull(user.getIdToken(false).await().token) { "Firebase returned no ID token" }
            ChatCredentials(userId = user.uid, idToken = token)
        }
}
