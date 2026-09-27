package com.solvyx.backend.data.remote

import android.util.Log
import com.solvyx.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Log de peticiones HTTP solo en debug: método, ruta, status y duración.
 */
class ApiLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!BuildConfig.DEBUG) return chain.proceed(request)

        val path = request.url.encodedPath
        Log.d(TAG, "→ ${request.method} $path")
        val start = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            Log.d(TAG, "← ${response.code} $path (${elapsedMs(start)} ms)")
            response
        } catch (e: IOException) {
            Log.w(TAG, "✕ $path (${elapsedMs(start)} ms): ${e.javaClass.simpleName}")
            throw e
        }
    }

    private fun elapsedMs(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000

    companion object {
        const val TAG = "SolvyxApi"
    }
}
