package com.solvyx.backend.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.solvyx.backend.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val assistRepository: AssistRepository
) {

    suspend fun registrarConEmail(
        apodo: String,
        email: String,
        password: String,
        fechaNacimiento: String
    ): Result<FirebaseUser> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user
            ?: return Result.failure(Exception("Algo salió mal. Intenta de nuevo."))
        crearPerfilFirestore(user.uid, apodo, email, fechaNacimiento)
        actualizarSesionLocal(
            serverId = user.uid,
            apodo = apodo,
            email = email,
            esAnonimo = false,
            fechaNacimiento = fechaNacimiento
        )
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun iniciarSesion(email: String, password: String): Result<FirebaseUser> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user
            ?: return Result.failure(Exception("Algo salió mal. Intenta de nuevo."))
        val doc = firestore.collection("users").document(user.uid).get().await()
        val fechaNacimientoRemota = doc.getTimestamp("fecha_nacimiento")?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX")).format(it.toDate())
        }
        @Suppress("UNCHECKED_CAST")
        val sustanciasRemotas = doc.get("sustancias_seleccionadas") as? List<String> ?: emptyList()
        actualizarSesionLocal(
            serverId = user.uid,
            apodo = doc.getString("apodo"),
            email = user.email,
            esAnonimo = false,
            fechaNacimiento = fechaNacimientoRemota,
            sustancias = sustanciasRemotas
        )
        assistRepository.hidratarDesdeServidor()
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun entrarComoAnonimo(): Result<FirebaseUser> = try {
        val result = firebaseAuth.signInAnonymously().await()
        val user = result.user
            ?: return Result.failure(Exception("Algo salió mal. Intenta de nuevo."))
        actualizarSesionLocal(serverId = user.uid, esAnonimo = true)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun enviarRecuperacionContrasena(email: String): Result<Unit> = try {
        firebaseAuth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    fun cerrarSesion() {
        firebaseAuth.signOut()
    }

    fun usuarioActual(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun convertirAnonimoAEmail(
        apodo: String,
        email: String,
        password: String,
        fechaNacimiento: String
    ): Result<FirebaseUser> = try {
        val currentUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("No hay una sesión anónima activa."))
        val sustanciasLocales = userRepository.observar().first()
            ?.sustanciasJson?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        val ultimoAssistLocal = assistRepository.observarUltimo().first()
        val assistCompletadoLocal = (ultimoAssistLocal?.totalCompletados ?: 0) > 0
        val credential = EmailAuthProvider.getCredential(email, password)
        val result = currentUser.linkWithCredential(credential).await()
        val user = result.user
            ?: return Result.failure(Exception("Algo salió mal. Intenta de nuevo."))
        crearPerfilFirestore(
            uid = user.uid,
            apodo = apodo,
            email = email,
            fechaNacimiento = fechaNacimiento,
            sustanciasIniciales = sustanciasLocales,
            assistCompletadoInicial = assistCompletadoLocal
        )
        actualizarSesionLocal(
            serverId = user.uid,
            apodo = apodo,
            email = email,
            esAnonimo = false,
            fechaNacimiento = fechaNacimiento
        )
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun assistCompletado(uid: String): Boolean = try {
        firestore.collection("users").document(uid).get().await()
            .getBoolean("assist_completado") ?: false
    } catch (e: Exception) {
        false
    }

    suspend fun actualizarSustancias(sustancias: Set<String>): Result<Unit> = try {
        val user = firebaseAuth.currentUser
            ?: return Result.failure(Exception("No hay sesión activa."))
        if (!user.isAnonymous) {
            firestore.collection("users").document(user.uid)
                .set(mapOf("sustancias_seleccionadas" to sustancias.toList()), SetOptions.merge())
                .await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun actualizarPerfil(apodo: String, fechaNacimiento: String): Result<Unit> = try {
        val user = firebaseAuth.currentUser
            ?: return Result.failure(Exception("No hay sesión activa."))
        if (!user.isAnonymous) {
            firestore.collection("users").document(user.uid)
                .set(
                    mapOf(
                        "apodo" to apodo,
                        "fecha_nacimiento" to parseFechaNacimiento(fechaNacimiento)
                    ),
                    SetOptions.merge()
                ).await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    private suspend fun crearPerfilFirestore(
        uid: String,
        apodo: String,
        email: String,
        fechaNacimiento: String,
        sustanciasIniciales: List<String> = emptyList(),
        assistCompletadoInicial: Boolean = false
    ) {
        firestore.collection("users").document(uid).set(
            hashMapOf(
                "apodo" to apodo,
                "email" to email,
                "fecha_nacimiento" to parseFechaNacimiento(fechaNacimiento),
                "sustancias_seleccionadas" to sustanciasIniciales,
                "assist_completado" to assistCompletadoInicial,
                "es_anonimo" to false,
                "racha_actual" to 0,
                "mejor_racha" to 0,
                "creado_en" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    private fun parseFechaNacimiento(fechaNacimiento: String): Date? = try {
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX")).parse(fechaNacimiento)
    } catch (e: Exception) {
        null
    }

    private suspend fun actualizarSesionLocal(
        serverId: String,
        esAnonimo: Boolean,
        apodo: String? = null,
        email: String? = null,
        fechaNacimiento: String? = null,
        sustancias: List<String>? = null
    ) {
        val actual = userRepository.observar().first() ?: UserEntity()
        userRepository.guardar(
            actual.copy(
                serverId = serverId,
                apodo = apodo ?: actual.apodo,
                email = email ?: actual.email,
                esAnonimo = esAnonimo,
                fechaNacimiento = fechaNacimiento ?: actual.fechaNacimiento,
                sustanciasJson = sustancias?.joinToString(",") ?: actual.sustanciasJson
            )
        )
    }

    private fun mapAuthError(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidUserException -> "No encontramos una cuenta con ese correo."
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil, usa al menos 6 caracteres."
        is FirebaseNetworkException -> "Sin conexión a internet. Intenta de nuevo."
        else -> "Algo salió mal. Intenta de nuevo."
    }
}
