package com.encryptedchat.models.firebase

import kotlinx.serialization.Serializable

@Serializable
data class Messages(
	var message: String = "",
	var sender: String = "",
	var time: Long = -1
)
