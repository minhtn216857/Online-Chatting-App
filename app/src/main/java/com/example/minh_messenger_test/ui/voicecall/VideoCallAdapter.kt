package com.example.minh_messenger_test.ui.voicecall

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Adapter
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.databinding.ItemAudioCallBinding
import com.example.minh_messenger_test.ui.home.AccountAdapter

class VideoCallAdapter(
    private val friendsWithStatus: MutableList<Pair<Account, String>> = mutableListOf(),
    private val listener: OnItemClickListener

): RecyclerView.Adapter<VideoCallAdapter.ViewHolder>() {
    class ViewHolder(
        private val binding: ItemAudioCallBinding,
        private val listener: OnItemClickListener
    ): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(friendWithStatus: Pair<Account, String>){
            binding.textUsernameCall.text = friendWithStatus.first.displayName
            Glide.with(binding.imgAvatar)
                .load(friendWithStatus.first.imageUrl)
                .error(R.drawable.avartar_ace_fas)
                .into(binding.imgAvatar)
            binding.textStatus.text = friendWithStatus.second

            binding.apply {
                when(friendWithStatus.second){
                    "ONLINE" ->{
                        btnCallVideo.isVisible = true
                        btnCallAudio.isVisible = true
                        btnCallVideo.setOnClickListener{
                            listener.onVideoCallClicked(friendWithStatus.first.username)
                        }
                        btnCallAudio.setOnClickListener{
                            listener.onAudioCallClicked(friendWithStatus.first.username)
                        }
                    }
                    "OFFLINE" ->{
                        btnCallVideo.isVisible = false
                        btnCallAudio.isVisible = false
                        textStatus.setTextColor(R.color.black)
                    }
                    "IN_CALL" ->{
                        btnCallVideo.isVisible = false
                        btnCallAudio.isVisible = false
                    }
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateStatus(list: List<Pair<Account, String>>){
        Log.d("VideoCallAdapter", "Updating status with list: $list") // Log danh sách mới

        this.friendsWithStatus.clear()
        this.friendsWithStatus.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val adapterView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio_call, parent, false)
        val binding = ItemAudioCallBinding.bind(adapterView)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return friendsWithStatus.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friendWithStatus = friendsWithStatus[position]
        Log.d("FriendStatus", "Friend: ${friendWithStatus.first}, Status: ${friendWithStatus.second}")
        holder.bind(friendWithStatus)
    }

    interface OnItemClickListener {
        fun onVideoCallClicked(target: String)
        fun onAudioCallClicked(target: String)
    }
}


