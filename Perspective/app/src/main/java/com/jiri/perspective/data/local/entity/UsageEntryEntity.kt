package com.jiri.perspective.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(    //Room entita pro tabulku usage_entries
    tableName = "usage_entries",
    foreignKeys = [ //Tady používáme cizí klíč, to propojuje usage entry s konkrétním subem
        ForeignKey(
            entity = SubscriptionEntity::class, //UsageEntry je navázano Na Subs.
            parentColumns = ["id"],
            childColumns = ["subscriptionId"], //subscriptionId v Usage entry odkazuje na id v Subs
            onDelete = ForeignKey.CASCADE   //když se vymaže subscripiton, vymaže se i jeho usage
        )
    ],
    indices = [Index(value = ["subscriptionId"])]   //Nad SubsId bude index, abychom mohli jednoduše filtrovat
)
data class UsageEntryEntity(    //
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,   //primary key id se generuje automaticky
    val subscriptionId: Long,
    val date: Long,
    val usageCount: Int = 1,
    val note: String? = null
)

