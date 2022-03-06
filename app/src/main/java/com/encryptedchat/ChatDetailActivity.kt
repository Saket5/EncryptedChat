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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import com.encryptedchat.models.firebase.Chats as FirebaseChats
import com.encryptedchat.models.firebase.Messages as FirebaseMessages

class ChatDetailActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChatDetailBinding

	private lateinit var adapter: ChatDetailAdapter

	private var chat: Chats? = null

	private var messageList: ArrayList<MessageViewType> = arrayListOf()
	private var decryptedMessageList: ArrayList<MessageViewType> = arrayListOf()

	private var isUserAuthenticated = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityChatDetailBinding.inflate(layoutInflater)
		val view = binding.root

		setContentView(view)

		val toolbar = binding.toolbar
		setSupportActionBar(toolbar)

		chat = intent.getParcelableExtra(Constants.CHAT_BUNDLE_ITEM) as Chats?

		toolbar.apply {
			setNavigationIconTint(resources.getColor(R.color.white))
			setNavigationOnClickListener { onBackPressed() }
			chat?.otherUserName?.let { title = it }
		}

		adapter = ChatDetailAdapter(decryptedMessageList)

		binding.rvMessage.layoutManager = LinearLayoutManager(this)

		binding.rvMessage.adapter = adapter

		val sendBtn = binding.btnSend
		sendBtn.setOnClickListener {
			val text = binding.etMessage.text.toString()
			if (text.isNotEmpty()) {
				sendMessage(text)
			}
		}

		fetchChats()

		SecurityHelper.showAuthenticationScreen(
			this,
			object : BiometricPrompt.AuthenticationCallback() {
				override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
					super.onAuthenticationSucceeded(result)
					isUserAuthenticated = true

					fetchMessages()
					fetchDecryptedMessages()
				}

				override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
					super.onAuthenticationError(errorCode, errString)
					Toast.makeText(
						this@ChatDetailActivity,
						"Authentication Failed. Please try again later.",
						Toast.LENGTH_LONG
					).show()
				}

				override fun onAuthenticationFailed() {
					super.onAuthenticationFailed()
					Toast.makeText(
						this@ChatDetailActivity,
						"Authentication Failed. Please try again later.",
						Toast.LENGTH_LONG
					).show()
				}
			})
	}

	private fun fetchChats() {
		val oldChat = chat

		oldChat?.id?.let { chatId ->
			FirebaseDatabase.getInstance().getReference(Constants.REALTIME_DB_CHATS)
				.child(chatId)
				.addValueEventListener(object : ValueEventListener {
					override fun onDataChange(snapshot: DataSnapshot) {
						if (snapshot.exists()) {
							val firebaseChats = snapshot.getValue(FirebaseChats::class.java)
							firebaseChats?.let { firebaseChats ->
								if (oldChat.messageIds.size != firebaseChats.message_ids.size) {
									chat = Chats(
										chatId,
										firebaseChats.last_message_time,
										firebaseChats.message_ids,
										oldChat.user1,
										oldChat.user2,
										oldChat.otherUserName
									)

									if (isUserAuthenticated) {
										fetchMessages()
										fetchDecryptedMessages()
									}
								}
							}
						}
					}

					override fun onCancelled(error: DatabaseError) {
					}
				})
		}
	}

	private fun fetchMessages() {
		messageList.clear()

		FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
			chat?.messageIds?.let { messageIds ->
				for (messageId in messageIds) {
					FirebaseDatabase.getInstance()
						.getReference(Constants.REALTIME_DB_MESSAGES)
						.child(messageId)
						.get()
						.addOnCompleteListener { task ->
							if (task.isSuccessful && task.result.exists()) {
								val firebaseMessage =
									task.result.getValue(FirebaseMessages::class.java)

								firebaseMessage?.let {
									val messageType = when (firebaseMessage.sender) {
										currentUserId -> Constants.SENDER_VIEW_TYPE
										else -> Constants.RECEIVER_VIEW_TYPE
									}

									messageList.add(
										MessageViewType(
											Messages(
												messageId,
												firebaseMessage.message,
												firebaseMessage.sender,
												firebaseMessage.time
											),
											messageType
										)
									)
								}
							} else {
								Toast.makeText(
									this,
									task.exception?.message
										?: "Something went wrong. Please try again.",
									Toast.LENGTH_LONG
								).show()
							}
						}
				}
			}
		}
	}

	private fun fetchDecryptedMessages() {
		decryptedMessageList.clear()

		FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
			chat?.messageIds?.let { messageIds ->
				for (messageId in messageIds) {
					FirebaseDatabase.getInstance()
						.getReference(Constants.REALTIME_DB_OTHER_MESSAGES)
						.child(messageId)
						.get()
						.addOnCompleteListener { task ->
							if (task.isSuccessful && task.result.exists()) {
								val firebaseMessage =
									task.result.getValue(FirebaseMessages::class.java)

								firebaseMessage?.let {
									val messageType = when (firebaseMessage.sender) {
										currentUserId -> Constants.SENDER_VIEW_TYPE
										else -> Constants.RECEIVER_VIEW_TYPE
									}

									decryptedMessageList.add(
										MessageViewType(
											Messages(
												messageId,
												firebaseMessage.message,
												firebaseMessage.sender,
												firebaseMessage.time
											),
											messageType
										)
									)

									if (decryptedMessageList.size == messageIds.size) {
										sortMessagesByTime()
									}
								}
							} else {
								Toast.makeText(
									this,
									task.exception?.message
										?: "Something went wrong. Please try again.",
									Toast.LENGTH_LONG
								).show()
							}
						}
				}
			}
		}
	}

	private fun sortMessagesByTime() {
		decryptedMessageList.sortBy { it.message.time }
		adapter.notifyDataSetChanged()
	}

	private fun sendMessage(message: String) {
		val messageId = UUID.randomUUID().toString()

		val encryptedMessage =
			SecurityHelper.encrypt(SecurityHelper.getPublicKey(this), message) ?: ""

		val messageTime = Date().time

		FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
			chat?.let { oldChat ->
				val newMessageIds = oldChat.messageIds
				newMessageIds.add(messageId)

				chat = Chats(
					oldChat.id,
					messageTime,
					newMessageIds,
					oldChat.user1,
					oldChat.user2,
					oldChat.otherUserName
				)

				val firebaseEncryptedMessages =
					FirebaseMessages(encryptedMessage, currentUserId, messageTime)
				FirebaseDatabase.getInstance()
					.getReference(Constants.REALTIME_DB_MESSAGES) // Update messages db
					.child(messageId)
					.setValue(firebaseEncryptedMessages)
					.addOnCompleteListener { task ->
						if (task.isSuccessful) {
							val firebaseMessages =
								FirebaseMessages(message, currentUserId, messageTime)
							FirebaseDatabase.getInstance()
								.getReference(Constants.REALTIME_DB_OTHER_MESSAGES) // Update other messages db
								.child(messageId)
								.setValue(firebaseMessages)
								.addOnCompleteListener { task2 ->
									if (task2.isSuccessful) {
										val firebaseChats =
											FirebaseChats(
												messageTime,
												newMessageIds,
												oldChat.user1,
												oldChat.user2
											)
										FirebaseDatabase.getInstance()
											.getReference(Constants.REALTIME_DB_CHATS) // Update chats db
											.child(oldChat.id)
											.setValue(firebaseChats)
											.addOnCompleteListener { task3 ->
												if (task3.isSuccessful) {
													decryptedMessageList.add(
														MessageViewType(
															Messages(
																messageId,
																message,
																currentUserId,
																messageTime
															), Constants.SENDER_VIEW_TYPE
														)
													)
													messageList.add(
														MessageViewType(
															Messages(
																messageId,
																encryptedMessage,
																currentUserId,
																messageTime
															), Constants.SENDER_VIEW_TYPE
														)
													)
													binding.etMessage.text.clear()
													adapter.notifyItemInserted(decryptedMessageList.size)
												} else {
													Toast.makeText(
														this,
														task3.exception?.message
															?: "Something went wrong. Please try again.",
														Toast.LENGTH_LONG
													).show()
												}
											}
									} else {
										Toast.makeText(
											this,
											task2.exception?.message
												?: "Something went wrong. Please try again.",
											Toast.LENGTH_LONG
										).show()
									}
								}
						} else {
							Toast.makeText(
								this,
								task.exception?.message
									?: "Something went wrong. Please try again.",
								Toast.LENGTH_LONG
							).show()
						}
					}
			}
		}
	}
}