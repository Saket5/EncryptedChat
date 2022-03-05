package com.encryptedchat.models.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessageViewType (val message : Messages, val viewType: Int): Parcelable