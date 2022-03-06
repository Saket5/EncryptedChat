package com.encryptedchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.encryptedchat.models.local.MessageViewType
import java.text.SimpleDateFormat
import java.util.*

class ChatDetailAdapter(private val messageList: ArrayList<MessageViewType>) :
	RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			0 -> SenderViewHolder(
				LayoutInflater.from(parent.context)
					.inflate(R.layout.sender_view, parent, false)
			)
			else -> ReceiverViewHolder(
				LayoutInflater.from(parent.context)
					.inflate(R.layout.reciever_view, parent, false)
			)
		}
	}

	inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
		private val tvTime: TextView = itemView.findViewById(R.id.tv_time)

		fun bind(position: Int) {
			val model = messageList[position]
			tvMessage.text = model.message.message

			val date = Date(model.message.time)
			val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
			tvTime.text = sdf.format(date)
		}
	}

	inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
		private val tvTime: TextView = itemView.findViewById(R.id.tv_time)

		fun bind(position: Int) {
			val model = messageList[position]
			tvMessage.text = model.message.message

			val date = Date(model.message.time)
			val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
			tvTime.text = sdf.format(date)
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (getItemViewType(position) == Constants.SENDER_VIEW_TYPE) {
			(holder as SenderViewHolder).bind(position)
		} else {
			(holder as ReceiverViewHolder).bind(position)
		}
	}

	override fun getItemCount(): Int {
		return messageList.size
	}

	override fun getItemViewType(position: Int): Int {
		return messageList[position].viewType
	}
}