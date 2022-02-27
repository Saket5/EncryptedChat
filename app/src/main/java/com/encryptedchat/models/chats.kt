package com.encryptedchat.models

data class chats(
    val id: String,
    val lastMessageTime: Long,
    val messageIds: ArrayList<String>,
    val user1: String,
    val user2: String
)
