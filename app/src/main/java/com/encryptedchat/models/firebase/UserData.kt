package com.encryptedchat.models.firebase

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
	var name: String = "",
	var phone_number: String = "",
	var public_key: String = "",
	var user_chat_id: String = ""
)
