package com.jiri.perspective.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jiri.perspective.data.repository.SubscriptionRepository
import com.jiri.perspective.data.repository.SubscriptionWithUsageCount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

//Overview screen nic neukldáá spíš počítá věci pro overviewscreen


data class CategorySpending(    //Datový model pro spending podle kategorií
    val category: String,
    val totalPrice: Double
)

data class UsageChartItem(   //Datový model pro spending podle kategorií
    val name: String,
    val usageCount: Int
)

enum class OverviewInsightType {    // Typ overview insightu
    WORST_VALUE,
    MOST_USED,
    UNUSED_SUBSCRIPTIONS
}

data class OverviewInsight( // obecný typ pro insight, informace se vyberou podle požadavků kartičky
    val type: OverviewInsightType,
    val primaryText: String,
    val count: Int? = null,
    val price: Double? = null,
    val currency: String? = null,
    val exampleName: String? = null
)

data class OverviewUiState( //Kompletní stav celé overview obrazovky
    val activeSubscriptions: Int = 0,
    val totalActivePrice: Double = 0.0,
    val totalUsageCount: Int = 0,
    val categorySpending: List<CategorySpending> = emptyList(),
    val usageRanking: List<UsageChartItem> = emptyList(),
    val worstValueInsight: OverviewInsight? = null,
    val mostUsedInsight: OverviewInsight? = null,
    val unusedSubscriptionsInsight: OverviewInsight? = null
)

class OverviewViewModel(
    repository: SubscriptionRepository
) : ViewModel() {

    val uiState: StateFlow<OverviewUiState> =   //bascily vezmeme informace a přepočítáme to na dashboard
        repository.getAllSubscriptionsWithUsageCount()
            .map { subscriptions ->     //tohle to převede na OverviewUistate
                subscriptions.toOverviewUiState()
            }
            .stateIn(                   //tohle z toho udělá Stateflow
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = OverviewUiState()
            )

    private fun List<SubscriptionWithUsageCount>.toOverviewUiState(): OverviewUiState { //tohle vezme subscription a převede ho na overview state
        val activeItems = filter { it.isActive }    //vezmeme jenom aktivní itemy

        val categorySpending = activeItems
            .groupBy { subscription ->
                subscription.category.trim()
            }
            .map { (category, items) -> // seskupíme to podle category
                CategorySpending(
                    category = category,
                    totalPrice = items.sumOf { it.price }   // pro každou kategorii spočítá jejich celkouvou cenu
                )
            }
            .sortedByDescending { it.totalPrice }   //seřadíme

        val usageRanking = activeItems  // U těch barů tak to seskupíme podle opužití a necháme jich tam 5
            .sortedByDescending { it.usageCount }
            .take(5)
            .map {
                UsageChartItem(
                    name = it.name,
                    usageCount = it.usageCount
                )
            }

        val worstValueSubscription = activeItems        //nejhorší value
            .filter { it.usageCount > 0 }
            .maxByOrNull { it.price / it.usageCount.toDouble() }

        val mostUsedSubscription = activeItems          // nejlepší
            .filter { it.usageCount > 0 }
            .maxByOrNull { it.usageCount }

        val unusedSubscriptions = activeItems.filter { it.usageCount == 0 } // nepoužíváné subs

        val worstValueInsight = worstValueSubscription?.let {       //teďka z těch předešých věcí poskádáme insight
            OverviewInsight(
                type = OverviewInsightType.WORST_VALUE,
                primaryText = it.name,
                price = it.price / it.usageCount.toDouble(),
                currency = it.currency
            )
        }

        val mostUsedInsight = mostUsedSubscription?.let {
            OverviewInsight(
                type = OverviewInsightType.MOST_USED,
                primaryText = it.name,
                count = it.usageCount
            )
        }

        val unusedSubscriptionsInsight = OverviewInsight(
            type = OverviewInsightType.UNUSED_SUBSCRIPTIONS,
            primaryText = unusedSubscriptions.size.toString(),
            exampleName = unusedSubscriptions.firstOrNull()?.name
        )

        return OverviewUiState(     //celkový stav UI obrazovky
            activeSubscriptions = activeItems.size,
            totalActivePrice = activeItems.sumOf { it.price },
            totalUsageCount = activeItems.sumOf { it.usageCount },
            categorySpending = categorySpending,
            usageRanking = usageRanking,
            worstValueInsight = worstValueInsight,
            mostUsedInsight = mostUsedInsight,
            unusedSubscriptionsInsight = unusedSubscriptionsInsight
        )
    }
}

class OverviewViewModelFactory(
    private val repository: SubscriptionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
            return OverviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}