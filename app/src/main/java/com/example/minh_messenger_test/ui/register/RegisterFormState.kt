package com.example.minh_messenger_test.ui.register

class RegisterFormState(
    val usernameError: Int? = null,
    val emailError: Int? = null,
    val displayNameError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val avatarError: Int? = null,
    val isCorrect: Boolean = false
)