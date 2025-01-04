package com.example.minh_messenger_test.ui.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.source.DataSource
import com.example.minh_messenger_test.data.source.DefaultRepository
import com.example.minh_messenger_test.data.source.Repository
import com.example.minh_messenger_test.data.source.local.DefaultLocalDataSource
import com.example.minh_messenger_test.data.source.remote.DefaultRemoteDataSource
import com.example.minh_messenger_test.databinding.FragmentRegisterBinding
import com.example.minh_messenger_test.utils.MessengerUtils
import com.example.minh_messenger_test.utils.MessengerUtils.afterTextChanged
import com.example.minh_messenger_test.utils.MessengerUtils.defaultAvatarId
import com.google.android.material.snackbar.Snackbar

class RegisterFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private var avatarName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupViewModel()
    }

    private fun setupListener() {
        binding.includeRegister.imageView1.setOnClickListener(this)
        binding.includeRegister.imageView2.setOnClickListener(this)
        binding.includeRegister.imageView3.setOnClickListener(this)
        binding.includeRegister.imageView4.setOnClickListener(this)
        binding.includeRegister.imageView5.setOnClickListener(this)
        binding.includeRegister.imageView6.setOnClickListener(this)

        // Thiết lập sự kiện thay đổi văn bản cho trường nhập tên người dùng
        binding.editRegisterUsername.afterTextChanged {
            val display = binding.editRegisterDisplayname.text.toString()
            val email = binding.editRegisterEmail.text.toString()
            val password = binding.editRegisterPassword.text.toString()
            val confirmPwd = binding.editRegisterConfirmPassword.text.toString()
            // Gửi thông tin đến  ViewModel khi tên người dùng thay đổi
            viewModel.registerFormChanged(it, display, email, password, confirmPwd, avatarName)
        }
        binding.editRegisterEmail.afterTextChanged {
            val display = binding.editRegisterDisplayname.text.toString()
            val username = binding.editRegisterUsername.text.toString()
            val password = binding.editRegisterPassword.text.toString()
            val confirmPwd = binding.editRegisterConfirmPassword.text.toString()
            viewModel.registerFormChanged(username, display, it, password, confirmPwd, avatarName)
        }
        binding.editRegisterDisplayname.afterTextChanged {
            val email = binding.editRegisterEmail.text.toString()
            val username = binding.editRegisterUsername.text.toString()
            val password = binding.editRegisterPassword.text.toString()
            val confirmPwd = binding.editRegisterConfirmPassword.text.toString()
            viewModel.registerFormChanged(username, it, email, password, confirmPwd, avatarName)
        }
        binding.editRegisterPassword.afterTextChanged {
            val username = binding.editRegisterUsername.text.toString()
            val email = binding.editRegisterEmail.text.toString()
            val display = binding.editRegisterDisplayname.text.toString()
            val confirmPwd = binding.editRegisterConfirmPassword.text.toString()
            viewModel.registerFormChanged(username, display, email, it, confirmPwd, avatarName)
        }
        binding.editRegisterConfirmPassword.afterTextChanged {
            val username = binding.editRegisterUsername.text.toString()
            val email = binding.editRegisterEmail.text.toString()
            val display = binding.editRegisterDisplayname.text.toString()
            val password = binding.editRegisterPassword.text.toString()
            viewModel.registerFormChanged(username, display, email, password, it, avatarName)
        }

        binding.buttonRegister.setOnClickListener{
            val username = binding.editRegisterUsername.text.toString()
            val email = binding.editRegisterEmail.text.toString()
            val display = binding.editRegisterDisplayname.text.toString()
            val password = binding.editRegisterPassword.text.toString()
            viewModel.registerAccount(username, email, display, password, avatarName)
            binding.progressBarRegister.visibility = View.VISIBLE

        }

        binding.buttonCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as MessengerApplication).repository

        // Khởi tạo viewModel bằng ViewModelProvider, liên kết với activity hiện tại
        viewModel = ViewModelProvider(
            requireActivity(),
            RegisterViewModelFactory(repository))[RegisterViewModel::class.java]

        // Quan sát trạng thái của biểu mẫu đăng ký
        viewModel.registerFormState.observe(viewLifecycleOwner) {
            binding.buttonRegister.isEnabled = false

            // Nếu có lỗi liên quan đến tên người dùng
            if (it.usernameError != null) {
                // Hiển thị lỗi trên trường nhập tên người dùng
                binding.editRegisterUsername.error = getString(it.usernameError)
            } else if (it.displayNameError != null) {
                binding.editRegisterDisplayname.error = getString(it.displayNameError)
            } else if (it.emailError != null) {
                binding.editRegisterEmail.error = getString(it.emailError)
            } else if (it.passwordError != null) {
                binding.editRegisterPassword.error = getString(it.passwordError)
            } else if (it.confirmPasswordError != null) {
                binding.editRegisterConfirmPassword.error = getString(it.confirmPasswordError)
            } else if (it.avatarError != null) {
                val snackBar = Snackbar.make(
                    binding.root,
                    it.avatarError,
                    Snackbar.LENGTH_LONG
                )
                snackBar.show()
            } else {
                binding.buttonRegister.isEnabled = true
            }
        }

        viewModel.registerState.observe(viewLifecycleOwner){
            binding.progressBarRegister.visibility = View.GONE
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            if(it.compareTo("Success") == 0){
                // todo
            }
        }
    }

    override fun onClick(v: View?) {
        v?.let {
            val oldState = it.isSelected
            resetImageAvatar()
            it.isSelected = !oldState
            avatarName = if (it.isSelected) {
                getImageUrlFromViewId(it.id)
            } else {
                null
            }
        }
        val username = binding.editRegisterUsername.text.toString()
        val email = binding.editRegisterEmail.text.toString()
        val display = binding.editRegisterDisplayname.text.toString()
        val password = binding.editRegisterPassword.text.toString()
        val confirmPwd = binding.editRegisterConfirmPassword.text.toString()
        viewModel.registerFormChanged(username, display, email, password, confirmPwd, avatarName)

    }

    private fun getImageUrlFromViewId(id: Int): String? {
        val avatarId = when (id) {
            R.id.image_view_1 -> R.drawable.avartar_nami_fas
            R.id.image_view_2 -> R.drawable.avartar_zoro_fas
            R.id.image_view_3 -> R.drawable.avartar_ace_fas
            R.id.image_view_4 -> R.drawable.avartar_luffy_fas
            R.id.image_view_5 -> R.drawable.avartar_sanji_fas
            R.id.image_view_6 -> R.drawable.avartar_chopper_fas
            else -> 0
        }
        return MessengerUtils.defaultAvatars[avatarId]
            ?: MessengerUtils.defaultAvatars[defaultAvatarId]
    }

    private fun resetImageAvatar() {
        binding.includeRegister.imageView1.isSelected = false
        binding.includeRegister.imageView2.isSelected = false
        binding.includeRegister.imageView3.isSelected = false
        binding.includeRegister.imageView4.isSelected = false
        binding.includeRegister.imageView5.isSelected = false
        binding.includeRegister.imageView6.isSelected = false
    }
}