package com.example.minh_messenger_test.ui.updateFriend

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.databinding.FragmentAddFriendBinding
import com.example.minh_messenger_test.ui.home.HomeViewModel
import com.example.minh_messenger_test.ui.home.HomeViewModelFactory
import com.example.minh_messenger_test.ui.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFriendFragment : Fragment() {

    private lateinit var binding: FragmentAddFriendBinding
    private lateinit var homeViewModel: HomeViewModel
    private val currentNameAccount = LoginViewModel.currentAccount.value?.username.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()


        binding.btnAddFriend.setOnClickListener {
            val nameFriend = binding.editTextAddFriend.text.toString()
            if(currentNameAccount == nameFriend) {
                Toast.makeText(
                    requireActivity(),
                    "Tài khoản trùng với tài khoản của bạn",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                homeViewModel.addFriend(currentNameAccount, nameFriend)
                homeViewModel.loadFriendAccounts(currentNameAccount)
            }

        }
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as MessengerApplication).repository
        homeViewModel = ViewModelProvider(
            requireActivity(),
            HomeViewModelFactory(repository)
        )[HomeViewModel::class.java]
    }

}