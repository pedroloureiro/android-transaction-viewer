package com.qonto.transactionviewer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val id: String = "transaction_remote_key",
    val seed: String,
    val nextPage: Int,
)
