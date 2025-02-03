package com.example.minh_messenger_test.ui.login

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.source.Repository
import com.example.minh_messenger_test.utils.MessengerUtils
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.model.AccountStatus
import com.google.firebase.Firebase
import com.google.firebase.database.core.Repo
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _loginFormState = MutableLiveData<LoginFormState>()
    private val _loggedInAccount = MutableLiveData<Account?>()
    private val _loginState = MutableLiveData<LoginState>()

    val loginFormState: LiveData<LoginFormState> = _loginFormState
    val loggedInAccount: LiveData<Account?> = _loggedInAccount
    val loginState: LiveData<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = Account(
                username,
                password,
                token = MessengerUtils.token
            )
            val result = (repository as Repository.RemoteRepository).loginAccount(account)
            result?.let{
                _loginState.postValue(LoginState(it.username, true))
            }
            updateStatusLoginRealTimeDatabase(username)
            _loggedInAccount.postValue(result)
        }
    }

    fun updateAccount(account: Account){
        viewModelScope.launch(Dispatchers.IO){
            val result = (repository as Repository.RemoteRepository).updateAccount(account)
            if(result){
                (repository as Repository.LocalRepository).updateLocalAccount(account)
                _loggedInAccount.postValue(account)
            }
        }
    }

    private fun updateStatusLoginRealTimeDatabase(username: String){
        val databaseRef = Firebase.database.reference
        databaseRef.child(username).child("status").setValue(AccountStatus.ONLINE.name)
            .addOnSuccessListener {
                Log.d("RealtimeDB", "Success")
            }
            .addOnFailureListener{
                Log.d("RealtimeDB", "Failure")
            }
    }

    fun saveAccountToLocal(){
        viewModelScope.launch(Dispatchers.IO) {
            val localRepository = (repository as Repository.LocalRepository)
            val accToSave = loggedInAccount.value
            accToSave?.let {
                localRepository.insertAccount(it)
            }
        }
    }

    fun saveLoginState(sharedPref: SharedPreferences, state: Boolean = true) {
        val editor = sharedPref.edit()
        val accToSave = loggedInAccount.value
        editor.putString(LoginFragment.PREF_USERNAME, accToSave?.username)
        editor.putBoolean(LoginFragment.PREFF_LOGIN_STATE, state)
        editor.apply()
    }

    fun loginFormChanged(username: String, password: String) {
        if (username.length < 3) {
            _loginFormState.value = LoginFormState(usernameError = R.string.error_username)
        } else if (password.length < 6) {
            _loginFormState.value = LoginFormState(passwordError = R.string.error_password)
        } else {
            _loginFormState.value = LoginFormState(isCorrect = true)
        }
    }

    fun getLoggedInState(sharedPref: SharedPreferences){
        val username = sharedPref.getString(LoginFragment.PREF_USERNAME, null)
        val state = sharedPref.getBoolean(LoginFragment.PREFF_LOGIN_STATE, false)
        _loginState.postValue(LoginState(username, state))
    }

    fun loadLocalAccountInfo(username: String){
        viewModelScope.launch(Dispatchers.IO){
            val localRepository = (repository as Repository.LocalRepository)
            val result = localRepository.getAccount(username)
            _loggedInAccount.postValue(result)
            Log.e("LOCAL_ACCOUNT", "$result")
            updateCurrentAccount(result)
        }
    }



    fun updateLoginState(state: LoginState?) {
        if(state == null){
            _loginState.postValue(LoginState())
        }

    }

    companion object{
        private val _currentAccount = MutableLiveData<Account?>()

        val currentAccount: LiveData<Account?> = _currentAccount

        fun updateCurrentAccount(account: Account?){
            _currentAccount.postValue(account)
        }
    }
}

// Nếu  ViewModel co tham so thi khoi tao phai dung Factory
@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra nếu modelClass là RegisterViewModel
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            // Tạo và trả về instance của RegisterViewModel với repository
            return LoginViewModel(repository) as T
        }
        // Ném ra ngoại lệ nếu modelClass không phải là RegisterViewModel
        throw IllegalArgumentException("Argument must be class RegisterViewModel")
    }
}

