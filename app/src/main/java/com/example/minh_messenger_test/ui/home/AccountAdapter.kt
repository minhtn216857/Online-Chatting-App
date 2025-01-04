package com.example.minh_messenger_test.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.model.Mesagge
import com.example.minh_messenger_test.databinding.ItemAccountBinding
import com.example.minh_messenger_test.ui.login.LoginViewModel

class AccountAdapter(
    private val accounts: MutableList<Account> = mutableListOf(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    private val messageMap = mutableMapOf<Int, Mesagge>()

    class ViewHolder(
        private val binding: ItemAccountBinding,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friendAccount: Account, message: Mesagge?) {
            binding.textItemDisplayName.text = friendAccount.displayName
            Glide.with(binding.imageItemAvartar)
                .load(friendAccount.imageUrl)
                .error(R.drawable.avartar_ace_fas)
                .circleCrop()
                .into(binding.imageItemAvartar)

            Glide.with(binding.imageItemReadStatus)
                .load(friendAccount.imageUrl)
                .error(R.drawable.avartar_ace_fas)
                .circleCrop()
                .into(binding.imageItemReadStatus)

            if(message != null){
                val msg = if(message.isIncoming){
                    message.data.text
                }else{
                    "You: ${message.data.text}"
                }
            }else{
                binding.textItemLastMessage.text = ""
            }

            binding.root.setOnClickListener{
                listener.OnItemClick(friendAccount)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val adapterView =
            LayoutInflater.from(parent.context).
            inflate(R.layout.item_account, parent, false)
        val binding = ItemAccountBinding.bind(adapterView)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Lấy đối tượng friend từ danh sách accounts tại vị trí position
        val friend = accounts[position]
        // Lấy tên đăng nhập của tài khoản hiện tại từ LoginViewModel, nếu không có thì sử dụng chuỗi rỗng
        val currentAcc = LoginViewModel.currentAccount.value?.username ?: ""
        // Lấy tin nhắn từ messageMap dựa trên giá trị hash của currentAcc và friend.username
        val message = messageMap[hashCode(currentAcc, friend.username)]
        // Gọi phương thức bind của holder để liên kết dữ liệu friend và message với view
        holder.bind(friend, message)
    }


    fun updateAccounts(accounts: List<Account>){
        val oldSize = this.accounts.size
        val newSize = accounts.size
        this.accounts.clear()
        this.accounts.addAll(accounts)
        if(newSize > oldSize){
            notifyItemRangeChanged(oldSize, newSize - oldSize)
        }else{
            notifyItemRangeRemoved(newSize - 1, oldSize - newSize)
        }
    }

    fun updateMessage(messages: Set<Mesagge>){
        for(mess in messages){
            updateMessage(mess)
        }
    }

    fun updateMessage(mess: Mesagge){
        val loggedInUserName = LoginViewModel.currentAccount.value?.username!!
        val friendUsername = if(loggedInUserName == mess.sender){
            mess.receiver
        }else{
            mess.sender
        }
        val hashValue = hashCode(loggedInUserName, friendUsername)
        messageMap[hashValue] = mess
        val positionToNotify = accounts.indexOf(Account(friendUsername))
        notifyItemChanged(positionToNotify)
    }

    private fun hashCode(sender: String, receiver: String): Int{
        var result = sender.hashCode()
        result = result * 31 + receiver.hashCode()
        return result
    }

    interface OnItemClickListener {
        fun OnItemClick(account: Account)
    }
}