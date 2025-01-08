package com.example.minh_messenger_test.ui.chat

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.model.Data
import com.example.minh_messenger_test.data.model.Mesagge
import com.example.minh_messenger_test.data.model.MessageStatus
import com.example.minh_messenger_test.data.model.Notification
import com.example.minh_messenger_test.data.source.Repository
import com.example.minh_messenger_test.ui.home.HomeViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(
    val repository: Repository
):ViewModel() {

    private val _interactingAccount = MutableLiveData<Account?>()
    val interactingAccount: LiveData<Account?> = _interactingAccount

    fun updateInteractingAccount(account: Account?){
        _interactingAccount.postValue(account)
    }

    private val _photoUri = MutableLiveData<Uri?>()
    val photo: LiveData<Uri?> = _photoUri

    private var _photoMimeType: String? = null

    private var _listMessage = mutableListOf<Mesagge>()
    private var _message = MutableLiveData<List<Mesagge>>()

    val message: LiveData<List<Mesagge>> = _message


    // Hàm thiết lập ảnh đính kèm tin nhắn
    fun setPhoto(uri: Uri, mimeType: String) {
        _photoUri.value = uri // Lưu URI của ảnh
        _photoMimeType = mimeType // Lưu MIME type của ảnh
    }


    fun loadMessage(){
        val sender = LoginViewModel.currentAccount.value?.username
        val receiver = interactingAccount.value?.username
        if(sender != null && receiver != null){
            viewModelScope.launch(Dispatchers.IO){
                val mess =(repository as Repository.RemoteRepository).getChat(sender, receiver)
                _listMessage.clear()
                _listMessage.addAll(mess.reversed())
                _message.postValue(_listMessage)
            }
        }
    }

    fun sendMessage(text: String){
        viewModelScope.launch(Dispatchers.IO){
            val senderAccount = LoginViewModel.currentAccount
            val notification = Notification(senderAccount.value?.displayName!!, text)

            val photoUrl = if(_photoUri.value == null) null else _photoUri.value.toString()
            val data = Data(text, photoUrl, _photoMimeType)
            interactingAccount.value?.let {
                val message = Mesagge(
                    id = 0,
                    data = data,
                    notification = notification,
                    sender = senderAccount.value?.username!!,
                    receiver = interactingAccount.value?.username!!,
                    token = interactingAccount.value?.token,
                    status = MessageStatus.SENT
                )
                (repository as Repository.RemoteRepository).sendMessage(message)
                addMessage(message)
                _photoMimeType = null // Xóa thông tin MIME type sau khi gửi
                _photoUri.postValue(null) // Xóa URI ảnh sau khi gửi
            }


        }
    }

    private fun addMessage(message: Mesagge) {
        _listMessage.add(message)
        _message.postValue(_listMessage)
    }
}

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra nếu modelClass là HomeViewModel
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            // Tạo và trả về instance của HomeViewModel với repository
            return ChatViewModel(repository) as T
        }
        // Ném ra ngoại lệ nếu modelClass không phải là RegisterViewModel
        throw IllegalArgumentException("Argument must be class RegisterViewModel")
    }
}