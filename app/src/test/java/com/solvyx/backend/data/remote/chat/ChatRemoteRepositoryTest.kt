package com.solvyx.backend.data.remote.chat

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChatRemoteRepositoryTest {

    private lateinit var server: MockWebServer
    private var userId: String? = "uid-123"

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ── enviarMensaje: respuestas HTTP ───────────────────────────────────────

    @Test
    fun `200 with text returns Exito with respuesta and historialId`() = runTest {
        server.enqueue(json(200, """{"respuesta":"Hola, aquí estoy","historialId":5}"""))

        val result = repository().enviarMensaje("Hola")

        assertEquals(EnviarMensajeResult.Exito("Hola, aquí estoy", 5L), result)
    }

    @Test
    fun `200 with blank respuesta returns RespuestaVacia`() = runTest {
        server.enqueue(json(200, """{"respuesta":"  ","historialId":5}"""))

        assertEquals(EnviarMensajeResult.RespuestaVacia, repository().enviarMensaje("Hola"))
    }

    @Test
    fun `400 contenido no permitido returns ContenidoNoPermitido`() = runTest {
        server.enqueue(json(400, error(400, "El mensaje contiene contenido no permitido")))

        assertEquals(EnviarMensajeResult.ContenidoNoPermitido, repository().enviarMensaje("Hola"))
    }

    @Test
    fun `400 respuesta vacia returns RespuestaVacia even if the API adds accents`() = runTest {
        server.enqueue(json(400, error(400, "La respuesta generada está vacía")))

        assertEquals(EnviarMensajeResult.RespuestaVacia, repository().enviarMensaje("Hola"))
    }

    @Test
    fun `400 validation error returns ErrorInesperado`() = runTest {
        server.enqueue(json(400, error(400, "mensaje no puede superar los 600 caracteres")))

        assertEquals(EnviarMensajeResult.ErrorInesperado, repository().enviarMensaje("Hola"))
    }

    @Test
    fun `500 and 502 return ServidorNoDisponible`() = runTest {
        server.enqueue(json(500, error(500, "Ocurrio un error inesperado")))
        server.enqueue(json(502, error(502, "Error al comunicarse con DeepSeek")))
        val repository = repository()

        assertEquals(EnviarMensajeResult.ServidorNoDisponible, repository.enviarMensaje("Hola"))
        assertEquals(EnviarMensajeResult.ServidorNoDisponible, repository.enviarMensaje("Hola"))
    }

    @Test
    fun `unexpected status code returns ErrorInesperado`() = runTest {
        server.enqueue(json(404, error(404, "Not Found")))

        assertEquals(EnviarMensajeResult.ErrorInesperado, repository().enviarMensaje("Hola"))
    }

    @Test
    fun `200 with a body that is not JSON returns ErrorInesperado`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("<html>proxy error</html>"))

        assertEquals(EnviarMensajeResult.ErrorInesperado, repository().enviarMensaje("Hola"))
    }

    // ── enviarMensaje: fallos de red ─────────────────────────────────────────

    @Test
    fun `timeout returns ServidorNoDisponible`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = repository(readTimeoutMs = 300).enviarMensaje("Hola")

        assertEquals(EnviarMensajeResult.ServidorNoDisponible, result)
    }

    @Test
    fun `server down returns ServidorNoDisponible`() = runTest {
        val apagado = MockWebServer().apply { start() }
        val url = apagado.url("/")
        apagado.shutdown()

        assertEquals(EnviarMensajeResult.ServidorNoDisponible, repository(baseUrl = url).enviarMensaje("Hola"))
    }

    // ── enviarMensaje: validaciones locales y request ────────────────────────

    @Test
    fun `message longer than 600 chars is not sent`() = runTest {
        val result = repository().enviarMensaje("a".repeat(ChatRemoteRepository.MAX_CARACTERES + 1))

        assertEquals(EnviarMensajeResult.MensajeInvalido, result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `blank message is not sent`() = runTest {
        assertEquals(EnviarMensajeResult.MensajeInvalido, repository().enviarMensaje("   "))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `without a Firebase user returns SinSesion and sends nothing`() = runTest {
        userId = null

        assertEquals(EnviarMensajeResult.SinSesion, repository().enviarMensaje("Hola"))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `request sends uid, generic nombre and trimmed mensaje`() = runTest {
        server.enqueue(json(200, """{"respuesta":"ok","historialId":1}"""))

        repository().enviarMensaje("  me siento ansioso  ")

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/chat/mensaje", request.path)
        val body = Gson().fromJson(request.body.readUtf8(), MensajeRequestDto::class.java)
        assertEquals(MensajeRequestDto("uid-123", "Usuario", "me siento ansioso"), body)
    }

    // ── cerrarSesion ─────────────────────────────────────────────────────────

    @Test
    fun `cerrarSesion 200 returns Cerrada and sends the uid`() = runTest {
        server.enqueue(json(200, """{"historialId":7,"status":"CERRADO"}"""))

        val result = repository().cerrarSesion()

        assertEquals(CerrarSesionResult.Cerrada(7L), result)
        val request = server.takeRequest()
        assertEquals("/api/chat/cerrar-sesion", request.path)
        assertEquals(CerrarSesionRequestDto("uid-123"), Gson().fromJson(request.body.readUtf8(), CerrarSesionRequestDto::class.java))
    }

    @Test
    fun `cerrarSesion 404 returns SinSesionActiva`() = runTest {
        server.enqueue(json(404, error(404, "El usuario no tiene una sesion activa")))

        assertEquals(CerrarSesionResult.SinSesionActiva, repository().cerrarSesion())
    }

    @Test
    fun `cerrarSesion 502 returns Fallo`() = runTest {
        server.enqueue(json(502, error(502, "Error al comunicarse con DeepSeek")))

        assertEquals(CerrarSesionResult.Fallo, repository().cerrarSesion())
    }

    @Test
    fun `cerrarSesion without a Firebase user does not call the API`() = runTest {
        userId = null

        assertEquals(CerrarSesionResult.SinSesionActiva, repository().cerrarSesion())
        assertEquals(0, server.requestCount)
    }

    // ── servidorDisponible ───────────────────────────────────────────────────

    @Test
    fun `servidorDisponible is true when the server answers 200`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200))

        assertEquals(true, repository().servidorDisponible())
        assertEquals("/", server.takeRequest().path)
    }

    @Test
    fun `servidorDisponible is true even if the server answers an error`() = runTest {
        // La API responde 500 a la raíz (su manejador genérico); igual prueba que está encendida.
        server.enqueue(json(500, error(500, "Ocurrio un error inesperado")))

        assertEquals(true, repository().servidorDisponible())
    }

    @Test
    fun `servidorDisponible is false when the server is down`() = runTest {
        val apagado = MockWebServer().apply { start() }
        val url = apagado.url("/")
        apagado.shutdown()

        assertEquals(false, repository(baseUrl = url).servidorDisponible())
    }

    @Test
    fun `servidorDisponible is false when the server does not answer in time`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        assertEquals(false, repository(readTimeoutMs = 300).servidorDisponible())
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun repository(
        baseUrl: HttpUrl = server.url("/"),
        readTimeoutMs: Long = 5_000
    ): ChatRemoteRepository {
        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApi::class.java)
        return ChatRemoteRepository(api) { userId }
    }

    private fun json(code: Int, body: String): MockResponse =
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)

    private fun error(status: Int, mensaje: String): String =
        """{"timestamp":"2026-09-26T00:00:00","status":$status,"error":"Error","mensaje":"$mensaje"}"""
}
