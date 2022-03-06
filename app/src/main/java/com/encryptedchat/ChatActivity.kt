package com.encryptedchat

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.encryptedchat.databinding.ActivityChatBinding
import com.encryptedchat.models.firebase.UserChats
import com.encryptedchat.models.local.Chats
import com.encryptedchat.models.local.UserData
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import com.encryptedchat.models.firebase.Chats as FirebaseChats
import com.encryptedchat.models.firebase.UserData as FirebaseUserData

class ChatActivity : AppCompatActivity() {
	private lateinit var binding: ActivityChatBinding

	private lateinit var adapter: ChatAdapter

	private var currentUserData: UserData? = null

	private val chats: ArrayList<Chats> = arrayListOf()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityChatBinding.inflate(layoutInflater)
		val view = binding.root

		setContentView(view)

		currentUserData = intent.extras?.getParcelable(Constants.USER_DATA_BUNDLE_ITEM)
		currentUserData?.let {
			binding.tvUserId.text = it.userId
		}

		binding.ibCopyBtn.setOnClickListener {
			val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
			clipboardManager.setPrimaryClip(ClipData.newPlainText("User ID", binding.tvUserId.text))
			Toast.makeText(this, "Copied USER ID to clipboard", Toast.LENGTH_LONG).show()
		}

		val toolbar = binding.toolbar
		setSupportActionBar(toolbar)

		adapter = ChatAdapter(chats)

		binding.rvChats.layoutManager = LinearLayoutManager(this)

		binding.rvChats.adapter = adapter

		binding.skvProgressBar.visibility = View.VISIBLE

		getChats()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu_chat, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.add -> {
				showAddUserDialog()
			}
			R.id.logout -> {
				FirebaseAuth.getInstance().signOut()
				gotoLoginActivity()
			}
		}
		return false
	}

	private fun showAddUserDialog() {
		val dialog = AlertDialog.Builder(this)
			.setView(R.layout.dialog_add_chat)
			.create()

		dialog.show()

		val submitButton: MaterialButton = dialog.findViewById(R.id.btn_add_user_id)
		val userIdEt: EditText = dialog.findViewById(R.id.et_add_user_id)

		submitButton.setOnClickListener {
			dialog.dismiss()

			binding.skvProgressBar.visibility = View.VISIBLE

			val userId = userIdEt.text.toString()
			if (userId.isNotEmpty()) {
				checkUserExists(userId)
			} else {
				Toast.makeText(this, "User ID cannot be empty", Toast.LENGTH_LONG).show()
			}
		}
	}

	private fun getChats() {
		currentUserData?.let {
			FirebaseFirestore.getInstance().collection(Constants.FIRE_STORE_USER_CHATS)
				.document(it.chatId)
				.get()
				.addOnCompleteListener { task ->
					binding.skvProgressBar.visibility = View.GONE
					if (task.isSuccessful && task.result.exists()) {
						val userChats = task.result.toObject(UserChats::class.java)
						if (userChats != null) {
							populateChats(userChats.chat_ids)
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

	private fun populateChats(chatIds: ArrayList<String>) {
		chats.clear()

		for (chatId in chatIds) {
			FirebaseDatabase.getInstance().getReference(Constants.REALTIME_DB_CHATS)
				.child(chatId)
				.get()
				.addOnCompleteListener { task ->
					if (task.isSuccessful && task.result.exists()) {
						val firebaseChats = task.result.getValue(FirebaseChats::class.java)
						if (firebaseChats != null && currentUserData != null) {
							val tempChats = Chats(
								chatId,
								firebaseChats.last_message_time,
								firebaseChats.message_ids,
								firebaseChats.user1,
								firebaseChats.user2,
								""
							)

							if (currentUserData!!.userId != tempChats.user1) {
								FirebaseFirestore.getInstance()
									.collection(Constants.FIRE_STORE_USER_DATA)
									.document(tempChats.user1)
									.get()
									.addOnCompleteListener { task2 ->
										if (task2.isSuccessful && task2.result.exists()) {
											val otherUser =
												task2.result.toObject(FirebaseUserData::class.java)
											if (otherUser != null) {
												tempChats.otherUserName = otherUser.name

												chats.add(tempChats)

												adapter.notifyItemInserted(chats.size)
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
							} else if (currentUserData!!.userId != tempChats.user2) {
								FirebaseFirestore.getInstance()
									.collection(Constants.FIRE_STORE_USER_DATA)
									.document(tempChats.user2)
									.get()
									.addOnCompleteListener { task2 ->
										if (task2.isSuccessful && task2.result.exists()) {
											val otherUser =
												task2.result.toObject(FirebaseUserData::class.java)
											if (otherUser != null) {
												tempChats.otherUserName = otherUser.name

												chats.add(tempChats)

												adapter.notifyItemInserted(chats.size)
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
							}
						}
					} else {
						Toast.makeText(
							this@ChatActivity,
							task.exception?.message ?: "Something went wrong. Please try again.",
							Toast.LENGTH_SHORT
						).show()
					}
				}
		}
	}

	private fun checkUserExists(userId: String) {
		FirebaseAuth.getInstance().currentUser?.let {
			FirebaseFirestore.getInstance().collection(Constants.FIRE_STORE_USER_DATA)
				.document(userId)
				.get()
				.addOnCompleteListener { task ->
					binding.skvProgressBar.visibility = View.GONE

					if (task.isSuccessful && task.result.exists()) {
						task.result.toObject(FirebaseUserData::class.java)?.apply {
							val otherUser = UserData(
								userId,
								this.name,
								this.phone_number,
								this.public_key,
								this.user_chat_id
							)
							createChatWithUser(otherUser)
						}
					} else {
						Toast.makeText(
							this,
							task.exception?.message ?: "User Id does not exists",
							Toast.LENGTH_LONG
						).show()
					}
				}
		}
	}

	private fun createChatWithUser(otherUser: UserData) {
		currentUserData?.let { currentUser -> // Check current user exists
			val chatId = UUID.randomUUID().toString()
			val firebaseChat =
				FirebaseChats(-1, arrayListOf(), currentUser.userId, otherUser.userId)
			FirebaseDatabase.getInstance()
				.getReference(Constants.REALTIME_DB_CHATS) // Create new chat in DB
				.child(chatId)
				.setValue(firebaseChat)
				.addOnCompleteListener { task ->
					if (task.isSuccessful) {
						val currentUserChatIds = ArrayList<String>()
						chats.forEach { currentUserChatIds.add(it.id) }
						currentUserChatIds.add(chatId)
						val currentUserChats = UserChats(currentUserChatIds)
						FirebaseFirestore.getInstance() // Add chat id to current user's chat
							.collection(Constants.FIRE_STORE_USER_CHATS)
							.document(currentUser.chatId)
							.set(currentUserChats)
							.addOnCompleteListener { task2 ->
								if (task2.isSuccessful) {
									FirebaseFirestore.getInstance() // Get all chat ids of other user
										.collection(Constants.FIRE_STORE_USER_CHATS)
										.document(otherUser.chatId)
										.get()
										.addOnCompleteListener { task3 ->
											if (task3.isSuccessful && task3.result.exists()) {
												val otherUserChats =
													task3.result.toObject(UserChats::class.java)
												otherUserChats?.let { otherUserChats ->
													otherUserChats.chat_ids.add(chatId)
													FirebaseFirestore.getInstance() // Add chat id to other user's chat
														.collection(Constants.FIRE_STORE_USER_CHATS)
														.document(otherUser.chatId)
														.set(otherUserChats)
														.addOnCompleteListener { task4 ->
															if (task4.isSuccessful) {
																val newChat = Chats(
																	chatId,
																	firebaseChat.last_message_time,
																	firebaseChat.message_ids,
																	firebaseChat.user1,
																	firebaseChat.user2,
																	otherUser.name
																)
																chats.add(newChat)
																adapter.notifyItemInserted(chats.size - 1)
																gotoChatDetailActivity(newChat)
															} else {
																Toast.makeText(
																	this@ChatActivity,
																	task4.exception?.message
																		?: "Something went wrong. Please try again.",
																	Toast.LENGTH_SHORT
																).show()
															}
														}
												}
											} else {
												Toast.makeText(
													this@ChatActivity,
													task3.exception?.message
														?: "Something went wrong. Please try again.",
													Toast.LENGTH_SHORT
												).show()
											}
										}
								} else {
									Toast.makeText(
										this@ChatActivity,
										task2.exception?.message
											?: "Something went wrong. Please try again.",
										Toast.LENGTH_SHORT
									).show()
								}
							}
					} else {
						Toast.makeText(
							this@ChatActivity,
							task.exception?.message ?: "Something went wrong. Please try again.",
							Toast.LENGTH_SHORT
						).show()
					}
				}
		}
	}

	private fun gotoLoginActivity() {
		val intent = Intent(this, LoginActivity::class.java)
		startActivity(intent)
		finish()
	}

	private fun gotoChatDetailActivity(chat: Chats) {
		val intent = Intent(this, ChatDetailActivity::class.java)
		intent.putExtra(Constants.CHAT_BUNDLE_ITEM, chat)
		startActivity(intent)
	}
}