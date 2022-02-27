package com.encryptedchat.models.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chats(
	val id: String,
	var lastMessageTime: Long,
	val messageIds: ArrayList<String>,
	val user1: String,
	val user2: String,
	var otherUserName: String,
) : Parcelable