package com.example.minh_messenger_test.ui.register

import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.source.Repository
import com.example.minh_messenger_test.utils.MessengerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _registerFormState = MutableLiveData<RegisterFormState>()
    private val _registerState = MutableLiveData<String>()

    val registerFormState: LiveData<RegisterFormState> = _registerFormState
    val registerState: LiveData<String> = _registerState

    fun registerAccount(
        username: String,
        email: String,
        displayName: String,
        password: String,
        avatar: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = "${MessengerUtils.token}"
            val imageUrl = "${MessengerUtils.AVARTAR_BASE_URL}$avatar"
            val account = Account(
                username, password, email,
                displayName, null, token, imageUrl
            )
            val result = (repository as Repository.RemoteRepository).createAccount(account)
            _registerState.postValue(result)
        }
    }

    fun registerFormChanged(
        username: String,
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String,
        avatar: String?,
    ) {
        if (username.length < 3) {
            _registerFormState.value = RegisterFormState(usernameError = R.string.error_username)
        } else if (email.isEmpty() && !isEmailCorrectFormat(email)) {
            _registerFormState.value = RegisterFormState(emailError = R.string.error_email)
        } else if (displayName.isEmpty()) {
            _registerFormState.value =
                RegisterFormState(displayNameError = R.string.error_displa_name)
        } else if (password.length < 6) {
            _registerFormState.value = RegisterFormState(passwordError = R.string.error_password)
        } else if (!isConfirmPasswordCorrect(password, confirmPassword)) {
            _registerFormState.value =
                RegisterFormState(confirmPasswordError = R.string.error_confirmPwd)
        } else if (avatar == null) {
            _registerFormState.value = RegisterFormState(avatarError = R.string.error_avatar)
        } else {
            _registerFormState.value = RegisterFormState(isCorrect = true)
        }
    }

    private fun isConfirmPasswordCorrect(password: String, confirmPassword: String): Boolean {
        return password.compareTo(confirmPassword) == 0
    }

    private fun isEmailCorrectFormat(email: String): Boolean {
        return if (email.contains('@')) {
            PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            false
        }
    }
}

@Suppress("UNCHECKED_CAST")
class RegisterViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra nếu modelClass là RegisterViewModel
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            // Tạo và trả về instance của RegisterViewModel với repository
            return RegisterViewModel(repository) as T
        }
        // Ném ra ngoại lệ nếu modelClass không phải là RegisterViewModel
        throw IllegalArgumentException("Argument must be class RegisterViewModel")
    }
}
