package com.encryptedchat.models.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    val userId: String,
    val name: String,
    val phNumber: String,
    val publicKey: String,
    val chatId: String
): Parcelable
