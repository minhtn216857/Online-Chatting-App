package com.example.minh_messenger_test.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.databinding.FragmentHomeBinding
import com.example.minh_messenger_test.service.MainService
import com.example.minh_messenger_test.service.MainServiceRepository
import com.example.minh_messenger_test.ui.chat.ChatViewModel
import com.example.minh_messenger_test.ui.chat.ChatViewModelFactory
import com.example.minh_messenger_test.ui.login.LoginFragment
import com.example.minh_messenger_test.ui.login.LoginViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModelFactory
import com.example.minh_messenger_test.ui.voicecall.repository.MainRepository
import com.example.minh_messenger_test.utils.DataModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class HomeFragment : Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var chatViewModel: ChatViewModel
    @Inject
    lateinit var mainServiceRepository: MainServiceRepository
    private lateinit var currentUser: String

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
                homeViewModel.loadFriendAccounts(it.username)
                // load thong tin tai khoan da dang nhap tu local
                loginViewModel.loadLocalAccountInfo(it.username)
                currentUser = it.username


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

        setupSwipeToDismiss()
    }

    private fun setupSwipeToDismiss() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                showDeleteConfirmationDialog(position)
            }

        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerHome)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteConfirmationDialog(position: Int) {
        val friendUser = accountAdapter.getAccountAt(position).username
        AlertDialog.Builder(requireActivity())
            .setTitle("Hủy kết bạn ")
            .setMessage("Bạn có chắc muốn hủy kết bạn?")
            .setPositiveButton("Có"){_, _ ->
                unFriendUser(currentUser, friendUser)

            }
            .setNegativeButton("Hủy"){ dialog, _ ->
                dialog.dismiss()
                accountAdapter.notifyDataSetChanged()
            }
            .show()


    }

    private fun unFriendUser(currentUser: String, userFriend: String) {
        homeViewModel.unFriend(currentUser, userFriend)
        homeViewModel.loadFriendAccounts(currentUser)
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