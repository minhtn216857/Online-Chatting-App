package com.example.minh_messenger_test.ui.chat

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.core.app.PendingIntentCompat.send
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.disklrucache.DiskLruCache.Editor
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.databinding.FragmentChatBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatViewModel: ChatViewModel // Sử dụng Hilt để inject ViewModel
    private lateinit var navBar: BottomNavigationView
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupViews()
        setupActions()
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as MessengerApplication).repository
        chatViewModel = ViewModelProvider(
            requireActivity(),
            ChatViewModelFactory(repository))[ChatViewModel::class.java]
        chatViewModel.loadMessage()

        chatViewModel.interactingAccount.observe(viewLifecycleOwner){
            requireActivity().title = it?.displayName
            //todo
        }
        chatViewModel.message.observe(viewLifecycleOwner){
            chatAdapter.submitList(it)
            binding.recyclerMessage.scrollToPosition(it.size - 1)
        }

        chatViewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.imagePhoto.visibility = View.GONE
            } else {
                binding.imagePhoto.visibility = View.VISIBLE
                Glide.with(binding.imagePhoto).load(it).into(binding.imagePhoto)
            }
        }
    }

    private fun setupViews() {
        navBar = requireActivity().findViewById(R.id.bottom_nav)
        val imageUrl = chatViewModel.interactingAccount.value?.imageUrl
        chatAdapter = ChatAdapter(requireContext(), imageUrl) {}
        binding.recyclerMessage.adapter = chatAdapter
    }

    private fun setupActions() {
        binding.chatEditInput.setOnImageAddedListener{contentUri, mimeType, label ->
            chatViewModel.setPhoto(contentUri, mimeType)
            if(binding.chatEditInput.text.isNullOrBlank()){
                binding.chatEditInput.setText(label)
            }
        }

        binding.imageSend.setOnClickListener {
            send()
        }



        binding.chatEditInput.setOnEditorActionListener{_, actionId, _ ->
            if(actionId  == EditorInfo.IME_ACTION_SEND){
                send()
                true
            } else{
                false
            }
        }

        binding.chatEditInput.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && !v.hasFocus()) {
                binding.chatEditInput.clearFocus()
                closeKeyboard()
            } else {
                if (hasFocus) {
                    navBar.visibility = View.GONE
                }
            }
        }
    }

    private fun closeKeyboard() {
        navBar.visibility = View.VISIBLE
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }


    private fun send() {
        binding.chatEditInput.text?.let{ text ->
            if(text.isNotEmpty()){
                chatViewModel.sendMessage(text.toString())
                text.clear()
            }
        }


    }

}