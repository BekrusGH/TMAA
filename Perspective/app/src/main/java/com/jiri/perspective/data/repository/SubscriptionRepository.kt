package com.jiri.perspective.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jiri.perspective.data.local.dao.SubscriptionDao
import com.jiri.perspective.data.local.dao.UsageEntryDao
import com.jiri.perspective.data.local.entity.SubscriptionEntity
import com.jiri.perspective.data.local.entity.UsageEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val usageEntryDao: UsageEntryDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> {
        return subscriptionDao.getAllSubscriptions()
    }

    fun getAllSubscriptionsWithUsageCount(): Flow<List<SubscriptionWithUsageCount>> {
        return subscriptionDao.getAllSubscriptionsWithUsageCount()
    }

    suspend fun getSubscriptionById(id: Long): SubscriptionEntity? {
        return subscriptionDao.getSubscriptionById(id)
    }

    suspend fun insertSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.insertSubscription(subscription)
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.updateSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.deleteSubscription(subscription)
    }

    suspend fun insertUsageEntry(usageEntry: UsageEntryEntity) {
        usageEntryDao.insertUsageEntry(usageEntry)
    }

    fun getUsageCountForSubscription(subscriptionId: Long): Flow<Int> {
        return usageEntryDao.getUsageCountForSubscription(subscriptionId)
    }

    suspend fun removeSingleUsageEntry(subscriptionId: Long) {
        val latestEntry = usageEntryDao.getLatestUsageEntryForSubscription(subscriptionId) ?: return

        if (latestEntry.usageCount > 1) {
            usageEntryDao.updateUsageEntry(
                latestEntry.copy(
                    usageCount = latestEntry.usageCount - 1
                )
            )
        } else {
            usageEntryDao.deleteUsageEntry(latestEntry)
        }
    }

    suspend fun resetUsageEntries(subscriptionId: Long) {
        usageEntryDao.deleteAllUsageEntriesForSubscription(subscriptionId)
    }

    suspend fun backupSubscriptionsToCloud() {
        val subscriptions = subscriptionDao.getAllSubscriptionsWithUsageCountOnce()

        for (subscription in subscriptions) {
            val data = hashMapOf(
                "id" to subscription.id,
                "name" to subscription.name,
                "description" to subscription.description,
                "price" to subscription.price,
                "currency" to subscription.currency,
                "billingPeriod" to subscription.billingPeriod,
                "startDate" to subscription.startDate,
                "nextPaymentDate" to subscription.nextPaymentDate,
                "category" to subscription.category,
                "isActive" to subscription.isActive,
                "createdAt" to subscription.createdAt,
                "updatedAt" to subscription.updatedAt,
                "usageCount" to subscription.usageCount
            )

            firestore
                .collection("subscriptions_backup")
                .document(subscription.id.toString())
                .set(data)
                .await()
        }
    }
}