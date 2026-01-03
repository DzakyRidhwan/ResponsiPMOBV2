package com.pmob.projectakhirpemrogramanmobile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Purchase(
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val timestamp: Long = 0L,
    val status: String = "",
    val method: String = "",
    val orderId: String = "",
    val transactionId: String = "",
    val bookCover: String = ""
) : Parcelable


