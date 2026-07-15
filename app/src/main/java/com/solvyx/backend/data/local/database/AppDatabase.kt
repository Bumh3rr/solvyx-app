package com.solvyx.backend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.solvyx.backend.data.local.dao.JournalDao
import com.solvyx.backend.data.local.dao.SosContactDao
import com.solvyx.backend.data.local.dao.AchievementDao
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.dao.LastAssistDao
import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.entity.JournalEntity
import com.solvyx.backend.data.local.entity.SosContactEntity
import com.solvyx.backend.data.local.entity.Converters
import com.solvyx.backend.data.local.entity.AchievementEntity
import com.solvyx.backend.data.local.entity.PlanEntity
import com.solvyx.backend.data.local.entity.SosEventEntity
import com.solvyx.backend.data.local.entity.LastAssistEntity
import com.solvyx.backend.data.local.entity.UserEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        UserEntity::class,
        SosContactEntity::class,
        LastAssistEntity::class,
        JournalEntity::class,
        PlanEntity::class,
        AchievementEntity::class,
        SosEventEntity::class
    ],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sosContactDao(): SosContactDao
    abstract fun lastAssistDao(): LastAssistDao
    abstract fun journalDao(): JournalDao
    abstract fun planDao(): PlanDao
    abstract fun achievementDao(): AchievementDao
    abstract fun sosEventDao(): SosEventDao

    companion object {
        val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                listOf("racha_3", "racha_7", "racha_10", "racha_15", "racha_30").forEach { id ->
                    db.execSQL(
                        "INSERT INTO achievements (id, unlocked, unlockDate) VALUES ('$id', 0, NULL)"
                    )
                }
            }
        }
    }
}
