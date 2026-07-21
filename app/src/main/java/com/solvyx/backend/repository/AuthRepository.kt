package com.solvyx.backend.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.solvyx.backend.data.local.database.AppDatabase
import com.solvyx.backend.data.local.entity.PlanEntity
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.data.remote.datasource.UserRemoteDataSource
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userRepository: UserRepository,
    private val assistRepository: AssistRepository,
    private val progressRepository: ProgressRepository,
    private val planRepository: PlanRepository,
    private val appDatabase: AppDatabase,
) {

    suspend fun registerWithEmail(
        nickname: String,
        email: String,
        password: String,
        birthDate: String
    ): Result<FirebaseUser> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: return Result.failure(Exception("Algo salió mal. Intenta de nuevo."))
        userRemoteDataSource.createProfile(
            user.uid,
            UserRemoteDto(nickname = nickname, email = email, birthDate = birthDate)
        )
        updateLocalSession(serverId = user.uid, isAnonymous = false)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: return Result.failure(Exception("Algo salió mal. Intenta de nuevo."))
        val profile = userRemoteDataSource.getProfile(user.uid)
        updateLocalSession(
            serverId = user.uid,
            isAnonymous = false,
            substances = profile?.selectedSubstances
        )
        assistRepository.hydrateFromServer()
        progressRepository.hydrateAchievements()
        if (profile?.planGoalIndex != null && profile.planGoalAchievedToday != null) {
            planRepository.saveLocalOnly(
                PlanEntity(
                    goalIndex = profile.planGoalIndex,
                    goalAchievedToday = profile.planGoalAchievedToday,
                    date = profile.planDate ?: System.currentTimeMillis()
                )
            )
        }
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> = try {
        val result = firebaseAuth.signInAnonymously().await()
        val user =
            result.user ?: return Result.failure(Exception("Algo salió mal. Intenta de nuevo."))
        userRemoteDataSource.createProfile(user.uid, UserRemoteDto(isAnonymous = true))
        updateLocalSession(serverId = user.uid, isAnonymous = true)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        firebaseAuth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        withContext(Dispatchers.IO) {
            appDatabase.clearAllTables()
        }
        // `clearAllTables()` borra también las filas base de logros, y el SEED_CALLBACK de Room
        // solo corre al crear la DB. Se reponen aquí para que la siguiente sesión —incluida una
        // anónima, que nunca pasa por hydrateAchievements()— tenga logros que desbloquear.
        progressRepository.ensureAchievementsSeeded()
    }

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    suspend fun convertAnonymousToEmail(
        nickname: String,
        email: String,
        password: String,
        birthDate: String
    ): Result<FirebaseUser> = try {
        val currentFirebaseUser = firebaseAuth.currentUser
            ?: return Result.failure(Exception("No hay una sesión anónima activa."))
        val localSubstances = userRepository.observe().first()
            ?.substancesJson?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        val lastAssistLocal = assistRepository.observeLast().first()
        val localAssistCompleted = (lastAssistLocal?.totalCompleted ?: 0) > 0
        val credential = EmailAuthProvider.getCredential(email, password)
        val result = currentFirebaseUser.linkWithCredential(credential).await()
        val user =
            result.user ?: return Result.failure(Exception("Algo salió mal. Intenta de nuevo."))
        userRemoteDataSource.createProfile(
            user.uid,
            UserRemoteDto(
                nickname = nickname,
                email = email,
                birthDate = birthDate,
                selectedSubstances = localSubstances,
                assistCompleted = localAssistCompleted
            )
        )
        updateLocalSession(serverId = user.uid, isAnonymous = false)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun getProfile(): UserRemoteDto? {
        val user = firebaseAuth.currentUser ?: return null
        return userRemoteDataSource.getProfile(user.uid)
    }

    suspend fun isAssistCompleted(uid: String): Boolean =
        userRemoteDataSource.isAssistCompleted(uid)

    suspend fun updateSubstances(substances: Set<String>): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("No hay sesión activa."))
        // Un usuario anónimo no sincroniza a Firestore: solo funciones offline (ver Firebase.md).
        if (!user.isAnonymous) {
            userRemoteDataSource.updateSubstances(user.uid, substances)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    suspend fun updateProfile(nickname: String, birthDate: String): Result<Unit> = try {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("No hay sesión activa."))
        if (!user.isAnonymous) {
            userRemoteDataSource.updateProfile(user.uid, nickname, birthDate)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapAuthError(e)))
    }

    private suspend fun updateLocalSession(
        serverId: String,
        isAnonymous: Boolean,
        substances: List<String>? = null
    ) {
        val current = userRepository.observe().first() ?: UserEntity()
        userRepository.save(
            current.copy(
                serverId = serverId,
                isAnonymous = isAnonymous,
                substancesJson = substances?.joinToString(",") ?: current.substancesJson
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
