package com.example.minh_messenger_test.ui.voicecall

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore.Video
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.databinding.FragmentVoiceCallBinding
import com.example.minh_messenger_test.ui.login.LoginFragment
import com.example.minh_messenger_test.ui.login.LoginViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModelFactory
import com.example.minh_messenger_test.ui.voicecall.repository.MainRepository
import com.example.minh_messenger_test.utils.getCameraAndMicPermission
import com.google.firebase.Firebase
import com.google.firebase.database.database
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VoiceCallFragment : Fragment() {
    private lateinit var binding: FragmentVoiceCallBinding
    private lateinit var videoCallViewModel: VideoCallViewModel
    private lateinit var videoCallAdapter: VideoCallAdapter
    @Inject
    lateinit var mainRepository: MainRepository
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var navController: NavController
    private var username: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("VoiceCallFragment", "onCreateView called")

        binding = FragmentVoiceCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(LoginFragment.EXTRA_LOGIN_SUCCESS)
            .observe(currentBackStackEntry) {
                if (!it) {
                    navigateToLoginFragment()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lấy instance của ViewModel
        val repository = (requireActivity().application as MessengerApplication).repository
        val loginViewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]

        // Lắng nghe cập nhật từ LoginViewModel
        loginViewModel.loggedInAccount.observe(viewLifecycleOwner) { account ->
            if (account != null) {
                username = account.username
                Log.d("VoiceCallFragment", "Username updated: $username")
                setupRecycler()
                setupViewModel()
            } else {
                Log.e("VoiceCallFragment", "Username is null! Không thể tiếp tục")

            }
        }


    }

    private fun setupRecycler() {
        val listener = object : VideoCallAdapter.OnItemClickListener {
            override fun onVideoCallClicked(target: String) {
                Log.d("TARGET", "$target")
                (activity as AppCompatActivity).getCameraAndMicPermission {
                    mainRepository.sendConnectionRequest(
                        sender = username!!,
                        target,
                        true
                    ) {
                        if (it) {
                            //we have to start video call
                            //we wanna create an intent to move to call activity
                            startActivity(Intent(
                                requireActivity(),
                                VoiceCallActivity::class.java
                            ).apply {
                                putExtra("target", target)
                                putExtra("isVideoCall", true)
                                putExtra("isCaller", true)
                            })

                        }
                    }
                }
            }

            override fun onAudioCallClicked(target: String) {
                (activity as AppCompatActivity).getCameraAndMicPermission {
                    mainRepository.sendConnectionRequest(
                        sender = username!!,
                        target,
                        false
                    ) {
                        if (it) {
                            //we have to start video call
                            //we wanna create an intent to move to call activity
                            startActivity(Intent(
                                requireContext(),
                                VoiceCallActivity::class.java
                            ).apply {
                                putExtra("target", target)
                                putExtra("isVideoCall", false)
                                putExtra("isCaller", true)
                            })
                        }
                    }
                }
            }

        }
        videoCallAdapter = VideoCallAdapter(listener = listener)
        binding.recyclerVoiceCall.adapter = videoCallAdapter


    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as MessengerApplication).repository
        val databaseRef = Firebase.database.reference

        videoCallViewModel = ViewModelProvider(
            requireActivity(),
            VideoCallViewModelFactory(repository, databaseRef)
        )[VideoCallViewModel::class.java]

        loginViewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]

        loginViewModel.loginState.observe(viewLifecycleOwner) {
            if (!it.status || it.username == null) {
                navigateToLoginFragment()
            } else {
                videoCallViewModel.loadFriendWithStatus(username!!)
                videoCallViewModel.friendsAccWithStatus.observe(viewLifecycleOwner) {
                    Log.d("MainActivity", "subscribeObservers: $it")
                    videoCallAdapter.updateStatus(it)
                }

            }
        }
    }

    private fun navigateToLoginFragment() {
        val action = VoiceCallFragmentDirections.actionCallFragmentToLoginFragment()
        navController.navigate(action)
    }
}


