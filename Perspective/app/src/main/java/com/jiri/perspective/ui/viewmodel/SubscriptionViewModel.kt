package com.jiri.perspective.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jiri.perspective.data.local.entity.SubscriptionEntity
import com.jiri.perspective.data.local.entity.UsageEntryEntity
import com.jiri.perspective.data.repository.SubscriptionRepository
import com.jiri.perspective.data.repository.SubscriptionWithUsageCount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//Vrstva mezi UI a Subscription repository, UI nepracuje s repozitory na přímo, používá přes to viewmodel

class SubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    companion object { //fixní honoty pro UI
        private const val DEFAULT_CURRENCY = "CZK"
        private const val DEFAULT_BILLING_PERIOD = "Monthly"
    }

    val subscriptions: StateFlow<List<SubscriptionWithUsageCount>> =    //od repository dostaneme Subs, viewmodel ho předává jako state flow pro UI
        repository.getAllSubscriptionsWithUsageCount().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSubscription(    //přidání subs
        name: String,
        description: String,
        price: String,
        category: String
    ) {     //uděláme validaci
        val trimmedName = name.trim()
        val trimmedCategory = category.trim()
        val parsedPrice = price.toDoubleOrNull()

        if (trimmedName.isBlank() || trimmedCategory.isBlank() || parsedPrice == null || parsedPrice <= 0.0) {
            return
        }

        val now = System.currentTimeMillis()

        viewModelScope.launch { //když validace projde vložíme subs
            repository.insertSubscription(
                SubscriptionEntity(
                    name = trimmedName,
                    description = description.trim().ifBlank { null },
                    price = parsedPrice,
                    currency = DEFAULT_CURRENCY,
                    billingPeriod = DEFAULT_BILLING_PERIOD,
                    startDate = now,
                    nextPaymentDate = now,
                    category = trimmedCategory,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun updateSubscription( //podobné jako add, jenom zachová nějaká existující data
        subscriptionId: Long,
        name: String,
        description: String,
        price: String,
        category: String,
        startDate: Long,
        nextPaymentDate: Long,
        isActive: Boolean,
        createdAt: Long
    ) {     //trim odělává mezery na začátku a na konci textu
        val trimmedName = name.trim()
        val trimmedCategory = category.trim()
        val parsedPrice = price.toDoubleOrNull()

        if (trimmedName.isBlank() || trimmedCategory.isBlank() || parsedPrice == null || parsedPrice <= 0.0) {
            return
        }

        viewModelScope.launch {
            repository.updateSubscription(
                SubscriptionEntity(
                    id = subscriptionId,
                    name = trimmedName,
                    description = description.trim().ifBlank { null },
                    price = parsedPrice,
                    currency = DEFAULT_CURRENCY,
                    billingPeriod = DEFAULT_BILLING_PERIOD,
                    startDate = startDate,
                    nextPaymentDate = nextPaymentDate,
                    category = trimmedCategory,
                    isActive = isActive,
                    createdAt = createdAt,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }

    fun deleteSubscriptionById(subscriptionId: Long) {
        viewModelScope.launch {
            val subscription = repository.getSubscriptionById(subscriptionId)
            if (subscription != null) {
                repository.deleteSubscription(subscription)
            }
        }
    }

    fun addUsageEntry(subscriptionId: Long) {
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            repository.insertUsageEntry(
                UsageEntryEntity(
                    subscriptionId = subscriptionId,
                    date = now,
                    usageCount = 1,
                    note = null
                )
            )
        }
    }

    fun removeUsageEntry(subscriptionId: Long) {
        viewModelScope.launch {
            repository.removeSingleUsageEntry(subscriptionId)
        }
    }

    fun resetUsageEntries(subscriptionId: Long) {
        viewModelScope.launch {
            repository.resetUsageEntries(subscriptionId)
        }
    }

    suspend fun backupSubscriptionsToCloud() {
        repository.backupSubscriptionsToCloud()
    }
}

class SubscriptionViewModelFactory(     //Vytvoří Viewmodel
    private val repository: SubscriptionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            return SubscriptionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}