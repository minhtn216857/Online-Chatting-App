package com.example.minh_messenger_test.ui.voicecall

import android.provider.MediaStore.Video
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.source.Repository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class VideoCallViewModel @Inject constructor(
    private val repository: Repository,
    private val databaseReference: DatabaseReference
): ViewModel() {
    private val _friendAccWithStatus = MutableLiveData<List<Pair<Account, String>>>()
    val friendsAccWithStatus: LiveData<List<Pair<Account, String>>> = _friendAccWithStatus

    fun loadFriendWithStatus(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("VideoCallViewModel", "loadFriendWithStatus() called with username: $username")

            val friends = (repository as Repository.RemoteRepository).loadFriendAccounts(username)
            Log.d("VideoCallViewModel", "Friends loaded from repository: $friends")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("VideoCallViewModel", "Firebase snapshot received: ${snapshot.childrenCount}")

                    val friendStatusList = friends?.map { friend ->
                        val status = snapshot.child(friend.username).child("status").value.toString()
                        Log.d("VideoCallViewModel", "Friend: ${friend.username}, Status: $status")
                        friend to status
                    }

                    _friendAccWithStatus.postValue(friendStatusList!!)
                    Log.d("VideoCallViewModel", "LiveData updated with: $friendStatusList")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("VideoCallViewModel", "Firebase error: ${error.message}")
                }
            })
        }
    }

}

@Suppress("UNCHECKED_CAST")
class VideoCallViewModelFactory(
    private val repository: Repository,
    private val databaseReference: DatabaseReference
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(VideoCallViewModel::class.java)){
            return VideoCallViewModel(repository, databaseReference) as T
        }
        throw IllegalArgumentException("Argument must be class VideoCallViewModel")
    }
}