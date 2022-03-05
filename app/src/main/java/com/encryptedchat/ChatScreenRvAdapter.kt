package com.encryptedchat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.encryptedchat.models.local.MessageViewType
import com.encryptedchat.models.local.Messages

class ChatScreenRvAdapter (context : Context, messageList : ArrayList<MessageViewType>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val context : Context  = context
    var messageList : ArrayList<MessageViewType> = messageList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            0-> SenderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_view, parent, false))
            else -> RecieverViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_view, parent, false))
        }
    }

    inner class SenderViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

//        var message: TextView = itemView.findViewById(R.id.textView)
//        fun bind(position: Int) {
//            val recyclerViewModel = messageList[position]
//            message.text = recyclerViewModel.message.message
//        }
    }

    inner class RecieverViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

//        var message: TextView = itemView.findViewById(R.id.textView)
//        fun bind(position: Int) {
//            val recyclerViewModel = messageList[position]
//            message.text = recyclerViewModel.message.message
//        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (messageList[position].viewType === Constants.SENDER_VIEW_TYPE) {
//            (holder as SenderViewHolder).bind(position)
//        } else {
//            (holder as RecieverViewHolder).bind(position)
//        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return messageList[position].viewType
    }
}