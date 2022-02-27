package com.encryptedchat.models.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserChats(val id: String, val chat_ids: ArrayList<String>) : Parcelable
