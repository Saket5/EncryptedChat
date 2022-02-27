package com.encryptedchat.models.firebase

import kotlinx.serialization.Serializable

@Serializable
data class Chats(
	var last_message_time: Long = -1,
	var message_ids: ArrayList<String> = arrayListOf(),
	var user1: String = "",
	var user2: String = "",
)
