package com.jiri.perspective.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jiri.perspective.data.local.entity.SubscriptionEntity
import com.jiri.perspective.data.repository.SubscriptionWithUsageCount
import kotlinx.coroutines.flow.Flow

//tohleto slouží jako interface pro práci s informacemi z tabulky

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY name ASC") //dostaneme všechny subs, podle jména
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")    //dostaneme vsechyn subs podle id
    suspend fun getSubscriptionById(id: Long): SubscriptionEntity?

    @Insert
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query(
        """
    SELECT 
        s.id,
        s.name,
        s.description,
        s.price,
        s.currency,
        s.billingPeriod,
        s.startDate,
        s.nextPaymentDate,
        s.category,
        s.isActive,
        s.createdAt,
        s.updatedAt,
        COALESCE(SUM(u.usageCount), 0) AS usageCount
    FROM subscriptions s
    LEFT JOIN usage_entries u ON s.id = u.subscriptionId
    GROUP BY s.id
    ORDER BY s.createdAt DESC
    """
    )
    fun getAllSubscriptionsWithUsageCount(): Flow<List<SubscriptionWithUsageCount>>

    @Query("""
    SELECT 
        s.id,
        s.name,
        s.description,
        s.price,
        s.currency,
        s.billingPeriod,
        s.startDate,
        s.nextPaymentDate,
        s.category,
        s.isActive,
        s.createdAt,
        s.updatedAt,
        COALESCE(SUM(u.usageCount), 0) AS usageCount
    FROM subscriptions s
    LEFT JOIN usage_entries u ON s.id = u.subscriptionId
    GROUP BY s.id
    ORDER BY s.createdAt DESC
""")
    suspend fun getAllSubscriptionsWithUsageCountOnce(): List<SubscriptionWithUsageCount>
}