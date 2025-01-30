package com.example.minh_messenger_test.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.databinding.FragmentHomeBinding
import com.example.minh_messenger_test.service.MainServiceRepository
import com.example.minh_messenger_test.ui.chat.ChatViewModel
import com.example.minh_messenger_test.ui.chat.ChatViewModelFactory
import com.example.minh_messenger_test.ui.login.LoginFragment
import com.example.minh_messenger_test.ui.login.LoginViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModelFactory
import com.example.minh_messenger_test.ui.voicecall.repository.MainRepository
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var chatViewModel: ChatViewModel
    @Inject
    lateinit var mainServiceRepository: MainServiceRepository




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getString(R.string.title_chat)
        navController = findNavController()
        // Lấy NavController từ Fragment hoặc Activity
        navController = findNavController()
        // Lấy Entry hiện tại từ BackStack của NavController
        val currentBackStackEntry = navController.currentBackStackEntry!!
        // Lấy SavedStateHandle từ BackStackEntry hiện tại
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        // Lắng nghe thay đổi trên LiveData chứa trạng thái đăng nhập thành công
        savedStateHandle.getLiveData<Boolean>(LoginFragment.EXTRA_LOGIN_SUCCESS)
            .observe(currentBackStackEntry) {
                // Nếu đăng nhập không thành công, điều hướng đến LoginFragment
                if (!it) {
                    navigateToLoginFragment()
                }
            }
    }

    private fun startMyService(username: String) {
//        val username = LoginViewModel.currentAccount.value!!.username
        Log.d("started", "started")
        mainServiceRepository.startService(username)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecycler()
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as MessengerApplication).repository

        homeViewModel = ViewModelProvider(
            requireActivity(),
            HomeViewModelFactory(repository)
        )[HomeViewModel::class.java]

        chatViewModel = ViewModelProvider(
            requireActivity(),
            ChatViewModelFactory(repository)
        )[ChatViewModel::class.java]

        loginViewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]

        loginViewModel.loginState.observe(viewLifecycleOwner) {
            if (!it.status || it.username == null) {
                navigateToLoginFragment()
            } else {
                Log.d("username", "${it.username}")
                homeViewModel.loadFriendAccounts(it.username)
                // load thong tin tai khoan da dang nhap tu local
                loginViewModel.loadLocalAccountInfo(it.username)
                startMyService(it.username)


            }
        }
        homeViewModel.friendAccounts.observe(viewLifecycleOwner) {
            accountAdapter.updateAccounts(it)
            binding.progressBarHome.visibility = View.GONE
        }
    }

    private fun setupRecycler() {
        val listener = object : AccountAdapter.OnItemClickListener {
            override fun OnItemClick(account: Account) {
                val loggedInAccount = loginViewModel.loggedInAccount.value
                if (loggedInAccount?.username != null) {
                    chatViewModel.updateInteractingAccount(account)
                    val action = HomeFragmentDirections.actionHomeFragmentToChatFragment()
                    navController.navigate(action)
                }else{
                    Snackbar.make(
                        requireView(),
                        R.string.error_logged_in,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        accountAdapter = AccountAdapter(listener = listener)
        binding.recyclerHome.adapter = accountAdapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.recyclerHome.addItemDecoration(divider)
        binding.progressBarHome.visibility = View.VISIBLE

    }

    private fun navigateToLoginFragment() {
        val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
        navController.navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}