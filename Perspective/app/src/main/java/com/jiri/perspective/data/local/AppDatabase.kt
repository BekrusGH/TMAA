package com.jiri.perspective.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jiri.perspective.data.local.dao.SubscriptionDao
import com.jiri.perspective.data.local.dao.UsageEntryDao
import com.jiri.perspective.data.local.entity.SubscriptionEntity
import com.jiri.perspective.data.local.entity.UsageEntryEntity

@Database(  //databáze má tyto dvě entity
    entities = [SubscriptionEntity::class, UsageEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //DAO metody
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun usageEntryDao(): UsageEntryDao

    companion object {  //držíme jednu sdílenou instanci
        @Volatile   //ochrana pro práci z více vlákend
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {    //když existuje databáze tak jí vratí, jinak novou vytvoří
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "perspective_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}