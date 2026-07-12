package com.solvyx.di

import com.solvyx.backend.repository.BitacoraExtendidaRepository
import com.solvyx.backend.repository.BitacoraExtendidaRepositoryImpl
import com.solvyx.backend.repository.EjerciciosRepository
import com.solvyx.backend.repository.EjerciciosRepositoryImpl
import com.solvyx.backend.repository.GuiasExtendidasRepository
import com.solvyx.backend.repository.GuiasExtendidasRepositoryImpl
import com.solvyx.backend.repository.JournalingRepository
import com.solvyx.backend.repository.JournalingRepositoryImpl
import com.solvyx.backend.repository.LeccionesRepository
import com.solvyx.backend.repository.LeccionesRepositoryImpl
import com.solvyx.backend.repository.PromptJournalingRepository
import com.solvyx.backend.repository.PromptJournalingRepositoryImpl
import com.solvyx.backend.repository.RutinasRepository
import com.solvyx.backend.repository.RutinasRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bindings Hilt para los repositorios del módulo offline.
 *
 * Se usa `@Binds` (no `@Provides`) porque es más eficiente: el cuerpo
 * del método solo necesita la firma, no lógica de construcción.
 * Todas las implementaciones ya son `@Singleton` por sí mismas, así
 * que la doble marca es redundante pero harmless.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEjerciciosRepository(impl: EjerciciosRepositoryImpl): EjerciciosRepository

    @Binds
    @Singleton
    abstract fun bindGuiasExtendidasRepository(impl: GuiasExtendidasRepositoryImpl): GuiasExtendidasRepository

    @Binds
    @Singleton
    abstract fun bindLeccionesRepository(impl: LeccionesRepositoryImpl): LeccionesRepository

    @Binds
    @Singleton
    abstract fun bindRutinasRepository(impl: RutinasRepositoryImpl): RutinasRepository

    @Binds
    @Singleton
    abstract fun bindPromptJournalingRepository(impl: PromptJournalingRepositoryImpl): PromptJournalingRepository

    @Binds
    @Singleton
    abstract fun bindJournalingRepository(impl: JournalingRepositoryImpl): JournalingRepository

    @Binds
    @Singleton
    abstract fun bindBitacoraExtendidaRepository(impl: BitacoraExtendidaRepositoryImpl): BitacoraExtendidaRepository
}
