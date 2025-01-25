package com.example.minh_messenger_test.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.source.Repository
import com.example.minh_messenger_test.ui.login.LoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    private val _friendAccounts = MutableLiveData<List<Account>>()
    val friendAccounts: LiveData<List<Account>> = _friendAccounts

    fun loadFriendAccounts(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = (repository as Repository.RemoteRepository).loadFriendAccounts(username)!!
            _friendAccounts.postValue(result)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra nếu modelClass là HomeViewModel
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Tạo và trả về instance của HomeViewModel với repository
            return HomeViewModel(repository) as T
        }
        // Ném ra ngoại lệ nếu modelClass không phải là RegisterViewModel
        throw IllegalArgumentException("Argument must be class RegisterViewModel")
    }
}
