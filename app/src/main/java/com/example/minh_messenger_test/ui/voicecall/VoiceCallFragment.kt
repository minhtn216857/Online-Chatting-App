package com.example.minh_messenger_test.ui.voicecall

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
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.source.Repository
import com.example.minh_messenger_test.databinding.FragmentVoiceCallBinding
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
//    private lateinit var loginViewModel: LoginViewModel
    @Inject lateinit var mainRepository: MainRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("VoiceCallFragment", "onCreateView called")

        binding = FragmentVoiceCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("VoiceCallFragment", "onViewCreated called")
        setupViewModel()
        setupRecycler()


    }

    private fun setupRecycler() {
        val listener = object: VideoCallAdapter.OnItemClickListener{
            override fun onVideoCallClicked(username: String) {
                (activity as AppCompatActivity).getCameraAndMicPermission {
                    mainRepository.sendConnectionRequest(username, true){
                        if(it){
                            //we have to start video call
                            //we wanna create an intent to move to call activity
                        }
                    }
                }
            }

            override fun onAudioCallClicked(username: String) {
                (activity as AppCompatActivity).getCameraAndMicPermission {
                    mainRepository.sendConnectionRequest(username, false){
                        if(it){
                            //we have to start video call
                            //we wanna create an intent to move to call activity
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
            VideoCallViewModelFactory(repository, databaseRef))[VideoCallViewModel::class.java]


        videoCallViewModel.loadFriendWithStatus(LoginViewModel.currentAccount.value!!.username)
        videoCallViewModel.friendsAccWithStatus.observe(viewLifecycleOwner){
            Log.d("MainActivity", "subscribeObservers: $it")
            videoCallAdapter.updateStatus(it)
        }
    }

}