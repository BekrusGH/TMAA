package com.jiri.perspective.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

//Tady máme dvě datové třídy, tohle slouží pro ROOM, tohle je basicly set-up dvou tabulek v databázi

@Entity(tableName = "subscriptions") //tohle říká, tadyta datová třída bude v tabulce subscriptoions
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null, //popis může být null -> ?
    val price: Double,
    val currency: String,       //tohle je fixed na CZK
    val billingPeriod: String,  //Fixed na monthly
    val startDate: Long,
    val nextPaymentDate: Long,
    val category: String,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

//Tohle to už není Room, tohle je "projekční datová třídy", bascily to kam se ulžotí výsledek nějaké dotazu
//Je to bascily Subscription, ale má navíc usagecount
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

//SubscripitonEntity je prostě set-up tabulky
//SubscriptionWithUsageCount je prostě setup pro place holder, pro výsledek nějákého query