package com.jiri.perspective.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jiri.perspective.data.local.entity.UsageEntryEntity
import kotlinx.coroutines.flow.Flow


//opět dotazovací soubor pro tabulku
@Dao
interface UsageEntryDao {

    //vrátí všechny záznamy usage přes jendu subscripiton
    @Query("SELECT * FROM usage_entries WHERE subscriptionId = :subscriptionId ORDER BY date DESC")
    fun getUsageEntriesForSubscription(subscriptionId: Long): Flow<List<UsageEntryEntity>>

        //vrátí latest sub usage
    @Query("""    
        SELECT * FROM usage_entries
        WHERE subscriptionId = :subscriptionId
        ORDER BY date DESC, id DESC
        LIMIT 1
    """)
    suspend fun getLatestUsageEntryForSubscription(subscriptionId: Long): UsageEntryEntity?

        //vložíme nový záznam, při konflitku přepíšeme existující řádek
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageEntry(usageEntry: UsageEntryEntity): Long

    @Update
    suspend fun updateUsageEntry(usageEntry: UsageEntryEntity)

    @Delete
    suspend fun deleteUsageEntry(usageEntry: UsageEntryEntity)

    //vymažeme všehchny UsageEntries, pro jedensub
    @Query("""
        DELETE FROM usage_entries
        WHERE subscriptionId = :subscriptionId
    """)
    suspend fun deleteAllUsageEntriesForSubscription(subscriptionId: Long)

        //COALESCE, když nejsou UsageEntries, vrátí se nula
    @Query("""
        SELECT COALESCE(SUM(usageCount), 0)
        FROM usage_entries
        WHERE subscriptionId = :subscriptionId
    """)
    fun getUsageCountForSubscription(subscriptionId: Long): Flow<Int>
}