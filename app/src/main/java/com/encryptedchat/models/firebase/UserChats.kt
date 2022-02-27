package com.encryptedchat.models.firebase

import kotlinx.serialization.Serializable

@Serializable
data class UserChats(var chat_ids: ArrayList<String> = arrayListOf())
