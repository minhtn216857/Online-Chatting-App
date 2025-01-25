package com.example.minh_messenger_test.ui.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.source.DataSource
import com.example.minh_messenger_test.data.source.DefaultRepository
import com.example.minh_messenger_test.data.source.Repository
import com.example.minh_messenger_test.data.source.local.DefaultLocalDataSource
import com.example.minh_messenger_test.data.source.remote.DefaultRemoteDataSource
import com.example.minh_messenger_test.databinding.FragmentLoginBinding
import com.example.minh_messenger_test.ui.register.RegisterViewModel
import com.example.minh_messenger_test.ui.register.RegisterViewModelFactory
import com.example.minh_messenger_test.utils.MessengerUtils.afterTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var navController: NavController
    private lateinit var savedStateHandle: SavedStateHandle
    private var isButtonLoginClicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        savedStateHandle = navController.previousBackStackEntry!!.savedStateHandle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupListener()

    }

    private fun setupListener() {
        binding.editLoginUsername.afterTextChanged {
            val password = binding.editLoginPassword.text.toString()
            viewModel.loginFormChanged(it, password)
        }
        binding.editLoginPassword.afterTextChanged {
            val username = binding.editLoginUsername.text.toString()
            viewModel.loginFormChanged(username, it)
        }

        binding.buttonLogin.setOnClickListener {
            isButtonLoginClicked = true
            val username = binding.editLoginUsername.text.toString()
            val password = binding.editLoginPassword.text.toString()
            viewModel.login(username, password)
            closeKeyboard()
            binding.progressBarLogin.visibility = View.VISIBLE
        }

        binding.buttonLoginRegister.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            navController.navigate(action)
        }
    }

    private fun closeKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as MessengerApplication).repository
        viewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]


        viewModel.loginFormState.observe(viewLifecycleOwner) {
            binding.buttonLogin.isEnabled = false

            if (it.usernameError != null) {
                binding.editLoginUsername.error = getString(it.usernameError)
            } else if (it.passwordError != null) {
                binding.editLoginPassword.error = getString(it.passwordError)
            } else {
                binding.buttonLogin.isEnabled = true
            }
        }

        // đăng ký giám sát tài khoản đang tiến hành đăng nhập
        viewModel.loggedInAccount.observe(viewLifecycleOwner) {
            if (isButtonLoginClicked) {
                if (it != null) {
                    isButtonLoginClicked = false
                    savedStateHandle[EXTRA_LOGIN_SUCCESS] = true
                    navController.popBackStack()
                } else {
                    Snackbar.make(
                        binding.root,
                        R.string.message_login_failed,
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
            }
            // ẩn progress bar đi
            binding.progressBarLogin.visibility = View.GONE
        }

    }

    override fun onStop() {
        super.onStop()
        viewModel.saveAccountToLocal()
        val sharedPref =
            (requireActivity().application as MessengerApplication).sharedReference
        viewModel.saveLoginState(sharedPref)
    }

    companion object {
        const val PREF_USERNAME = "PREF_USERNAME"
        const val PREFF_LOGIN_STATE = "PREFF_LOGIN_STATE"
        const val EXTRA_LOGIN_SUCCESS = "EXTRA_LOGIN_SUCCESS"
    }

}