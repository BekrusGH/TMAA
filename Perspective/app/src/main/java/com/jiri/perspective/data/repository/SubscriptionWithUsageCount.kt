package com.jiri.perspective.data.repository


//Datová třída na to abychom mohli namapovat subscription s UsageCount
//Je to bascily výsledek query
data class SubscriptionWithUsageCount(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val currency: String,
    val billingPeriod: String,
    val startDate: Long,
    val nextPaymentDate: Long,
    val category: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val usageCount: Int
)