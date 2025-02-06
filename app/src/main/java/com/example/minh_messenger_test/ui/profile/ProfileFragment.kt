package com.example.minh_messenger_test.ui.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.ViewModelProvider
import androidx.room.util.copy
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.databinding.BottomSheetEditProfileLayoutBinding
import com.example.minh_messenger_test.databinding.FragmentProfileBinding
import com.example.minh_messenger_test.ui.home.HomeViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModelFactory
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlin.contracts.contract

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupViews()

    }

    private fun setupViewModel(){
        val repository = (requireActivity().application as MessengerApplication).repository
        loginViewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]

    }


    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        loginViewModel.loggedInAccount.observe(viewLifecycleOwner){ currentAccount ->
            binding.textDisplayname.text = "Tên của bạn: ${currentAccount!!.displayName.toString()}"
            binding.textEmail.text = "Email: ${currentAccount.email.toString()}"

        }
        binding.btnEditProfile.setOnClickListener {
            showDialogEditProfile()
        }

    }

    private fun showDialogEditProfile() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = BottomSheetEditProfileLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.btnDone.setOnClickListener {
            val newDisplayName = dialogBinding.editNewDisplayName.text.toString()
            val newEmail = dialogBinding.editNewEmail.text.toString()

            loginViewModel.loggedInAccount.value?.let{currentAccount ->
                val updatedAccount =
                    currentAccount.copy(displayName = newDisplayName, email = newEmail)
                Log.d("ProfileFragment", "Updating account: $updatedAccount")
                Log.d("ProfileFragment", "New display name: ${updatedAccount.displayName}")

                loginViewModel.updateAccount(updatedAccount)
                loginViewModel.loadLocalAccountInfo(updatedAccount.username)

            }

            Snackbar.make(binding.root, "Completed", Snackbar.LENGTH_LONG).show()
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_bg)
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
}

