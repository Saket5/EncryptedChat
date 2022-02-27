package com.encryptedchat.models.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Messages(val id: String, val message: String, val sender: String, val time: Long) :
	Parcelable
