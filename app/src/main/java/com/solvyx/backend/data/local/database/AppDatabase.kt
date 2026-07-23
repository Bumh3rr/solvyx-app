package com.solvyx.backend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.solvyx.backend.data.local.dao.SosContactDao
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.dao.LastAssistDao
import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.entity.SosContactEntity
import com.solvyx.backend.data.local.entity.PlanEntity
import com.solvyx.backend.data.local.entity.SosEventEntity
import com.solvyx.backend.data.local.entity.LastAssistEntity
import com.solvyx.backend.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SosContactEntity::class,
        LastAssistEntity::class,
        PlanEntity::class,
        SosEventEntity::class
    ],
    version = 9
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sosContactDao(): SosContactDao
    abstract fun lastAssistDao(): LastAssistDao
    abstract fun planDao(): PlanDao
    abstract fun sosEventDao(): SosEventDao
}
