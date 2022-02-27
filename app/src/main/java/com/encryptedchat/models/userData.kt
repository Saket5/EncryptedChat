package com.encryptedchat.models

data class userData(
    val userId: String,
    val name: String,
    val phNumber: String,
    val publicKey: String,
    val chatId: String
)
