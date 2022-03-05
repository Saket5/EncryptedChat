package com.encryptedchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.encryptedchat.models.local.MessageViewType
import com.encryptedchat.models.local.Messages


class ChatDetailActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_chat_detail)
	}


//	fun transformViewType(message : Messages): MessageViewType {
//		var viewType =  Constants.RECIEVER_VIEW_TYPE
//		if ( message.sender.equals()){
//			viewType =Constants.SENDER_VIEW_TYPE
//		}
//		return MessageViewType (message ,viewType )
//	}
}