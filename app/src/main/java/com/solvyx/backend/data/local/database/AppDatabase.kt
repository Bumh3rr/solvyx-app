package com.solvyx.backend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.dao.ContactoSosDao
import com.solvyx.backend.data.local.dao.LogroDao
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.dao.ResultadoAssistDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import com.solvyx.backend.data.local.entity.Converters
import com.solvyx.backend.data.local.entity.LogroEntity
import com.solvyx.backend.data.local.entity.PlanEntity
import com.solvyx.backend.data.local.entity.ResultadoAssistEntity
import com.solvyx.backend.data.local.entity.SosEventEntity
import com.solvyx.backend.data.local.entity.UserEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        UserEntity::class,
        ContactoSosEntity::class,
        ResultadoAssistEntity::class,
        BitacoraEntity::class,
        PlanEntity::class,
        LogroEntity::class,
        SosEventEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactoSosDao(): ContactoSosDao
    abstract fun resultadoAssistDao(): ResultadoAssistDao
    abstract fun bitacoraDao(): BitacoraDao
    abstract fun planDao(): PlanDao
    abstract fun logroDao(): LogroDao
    abstract fun sosEventDao(): SosEventDao

    companion object {
        val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                listOf("racha_3", "racha_7", "racha_10", "racha_15", "racha_30").forEach { id ->
                    db.execSQL(
                        "INSERT INTO logros (id, unlocked, fechaUnlock) VALUES ('$id', 0, NULL)"
                    )
                }
            }
        }
    }
}
