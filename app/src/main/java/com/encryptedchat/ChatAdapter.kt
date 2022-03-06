package com.encryptedchat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.encryptedchat.models.local.Chats
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val chats: ArrayList<Chats>) :
	RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
		return ChatViewHolder(
			LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
		)
	}

	override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
		holder.bind(chats[position])
	}

	override fun getItemCount(): Int {
		return chats.size
	}

	inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
		View.OnClickListener {

		init {
			itemView.setOnClickListener(this)
		}

		private var chat: Chats? = null

		private var name: MaterialTextView = itemView.findViewById(R.id.item_chat_name)
		private var time: MaterialTextView = itemView.findViewById(R.id.item_chat_time)

		fun bind(chat: Chats) {
			this.chat = chat

			name.text = chat.otherUserName

			if (chat.lastMessageTime != -1L) {
				val date = Date(chat.lastMessageTime)
				val sdf = SimpleDateFormat("dd MMM\nHH:mm", Locale.getDefault())
				time.text = sdf.format(date)
			} else {
				time.text = "No message\nsent yet"
			}
		}

		override fun onClick(view: View?) {
			val intent = Intent(itemView.context, ChatDetailActivity::class.java)
			intent.putExtra(Constants.CHAT_BUNDLE_ITEM, this.chat)
			itemView.context.startActivity(intent)
		}
	}
}