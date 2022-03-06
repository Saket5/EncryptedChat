package com.encryptedchat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.recyclerview.widget.LinearLayoutManager
import com.encryptedchat.databinding.ActivityChatDetailBinding
import com.encryptedchat.models.local.Chats
import com.encryptedchat.models.local.MessageViewType
import com.encryptedchat.models.local.Messages

class ChatDetailActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChatDetailBinding

	private lateinit var adapter: ChatDetailAdapter

	private var chat: Chats? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityChatDetailBinding.inflate(layoutInflater)
		val view = binding.root

		setContentView(view)

		val toolbar = binding.toolbar
		setSupportActionBar(toolbar)

		chat = intent.getParcelableExtra(Constants.CHAT_BUNDLE_ITEM)

		toolbar.apply {
			setNavigationIconTint(resources.getColor(R.color.white))
			setNavigationOnClickListener { onBackPressed() }
			chat?.otherUserName?.let { title = it }
		}

		adapter = ChatDetailAdapter(arrayListOf())

		binding.rvMessage.layoutManager = LinearLayoutManager(this)

		binding.rvMessage.adapter = adapter

		val sendBtn = binding.btnSend
		sendBtn.setOnClickListener {
			sendMessage()
		}

		SecurityHelper.showAuthenticationScreen(
			this,
			object : BiometricPrompt.AuthenticationCallback() {
				override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
					super.onAuthenticationSucceeded(result)
				}
			})
	}

	private fun sendMessage() {

	}
}