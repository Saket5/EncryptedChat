package com.encryptedchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.encryptedchat.databinding.ActivityChatBinding
import com.encryptedchat.databinding.ActivityChatDetailBinding
import com.encryptedchat.models.local.MessageViewType
import com.encryptedchat.models.local.Messages
import com.google.android.material.appbar.MaterialToolbar


class ChatDetailActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChatDetailBinding

	private lateinit var adapter: ChatScreenRvAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityChatDetailBinding.inflate(layoutInflater)
		val view = binding.root

		setContentView(view)

		val toolbar = binding.toolbar
		setSupportActionBar(toolbar)

		toolbar.setNavigationOnClickListener{
			onBackPressed()
		}

		//adapter = ChatScreenRvAdapter(context,)

		binding.rvMessage.layoutManager = LinearLayoutManager(this)

		binding.rvMessage.adapter = adapter

		val sendBtn = binding.btnSend
		sendBtn.setOnClickListener {
			sendMessage()
		}



	}

	private fun sendMessage() {
		TODO("Not yet implemented")
	}


//	fun transformViewType(message : Messages): MessageViewType {
//		var viewType =  Constants.RECIEVER_VIEW_TYPE
//		if ( message.sender.equals()){
//			viewType =Constants.SENDER_VIEW_TYPE
//		}
//		return MessageViewType (message ,viewType )
//	}
}