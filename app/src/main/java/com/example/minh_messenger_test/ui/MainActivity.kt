package com.example.minh_messenger_test.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.minh_messenger_test.FCM.AccessToken
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.databinding.ActivityMainBinding
import com.example.minh_messenger_test.ui.login.LoginState
import com.example.minh_messenger_test.ui.login.LoginViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModelFactory
import com.example.minh_messenger_test.utils.MessengerUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var loginViewModel: LoginViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            postNotification()
        } else {
            showMessage(R.string.message_denied, Snackbar.LENGTH_LONG)
        }
    }

    private fun postNotification() {
//        TODO("Not yet implemented")
    }

    private fun showMessage(messageDenied: Int, duration: Int, showAction: Boolean = false) {
        val snackBar = Snackbar.make(binding.root, messageDenied, duration)
        if (showAction && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            snackBar.setAction("OK") {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            snackBar.setAction("No thank") {
                showMessage(R.string.message_denied, Snackbar.LENGTH_LONG)
            }
        }
        snackBar.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setupNavigation()
        setContentView(binding.root)
        askNotificationPermission()
        retrieveToken()
        setupViewModel()
        setupDrawerLayoutMenuItemSelectedListener()

    }

    private fun setupNavigation() {
        // Lấy NavHostFragment từ FragmentManager thông qua ID của nav_host_fragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // Lấy NavController từ NavHostFragment để quản lý điều hướng
        navController = navHostFragment.navController

        // Thiết lập AppBarConfiguration để cấu hình thanh AppBar (hoặc Toolbar)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.home_fragment, // ID của Home Fragment
                R.id.voiceCallFragment, // ID của Voice Call Fragment
                R.id.phoneBookFragment // ID của Phone Book Fragment
            ),
            drawerLayout = binding.drawerLayout // Thiết lập DrawerLayout nếu sử dụng Navigation Drawer
        )

        //  Thiết lập toolbar làm actionbar
        setSupportActionBar(binding.includeMain.toolbarMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.includeMain.toolbarMain.setupWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.login_fragment
                || destination.id == R.id.register_fragment
                || destination.id == R.id.chat_fragment
            ) {
                // Đặt icon navigation cho các fragment đăng nhập, đăng ký, và chat
                binding.includeMain.toolbarMain.setNavigationIcon(R.drawable.ic_up)
            }else if(destination.id == R.id.home_fragment
                || destination.id == R.id.voiceCallFragment
                || destination.id == R.id.phoneBookFragment){
                binding.includeMain.toolbarMain.setNavigationIcon(R.drawable.ic_drawer)
            }
        }

        binding.includeMain.bottomNav.setupWithNavController(navController)

    }

    //     Phương thức yêu cầu quyền thông báo
    private fun askNotificationPermission() {
        // Kiểm tra nếu phiên bản Android là TIRAMISU (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Kiểm tra xem quyền POST_NOTIFICATIONS đã được cấp chưa
            val selPms = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            // Kiểm tra nếu ứng dụng nên hiển thị lời nhắc yêu cầu quyền hay không
            val shouldShowPrompt =
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)

            // Nếu quyền đã được cấp, gọi phương thức postNotification
            if (selPms == PackageManager.PERMISSION_GRANTED) {
                postNotification()
            }
            // Nếu nên hiển thị lời nhắc, hiển thị thông báo cho người dùng
            else if (shouldShowPrompt) {
                showMessage(R.string.mesg_permission_prompt, Snackbar.LENGTH_LONG)
            }
            // Nếu không, khởi chạy yêu cầu quyền thông qua requestPermissionLauncher
            else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun retrieveToken() {
        CoroutineScope(Dispatchers.IO).launch {
            val token: String? = AccessToken().accessToken
            Handler(Looper.getMainLooper()).post {
                if (token != null) {
                    MessengerUtils.token = token
                    Log.e("Access Token: ", token)
                } else {
                    Log.e("Access Token: ", "Failed to  obtain access token")
                }
            }
        }
    }

    private fun setupViewModel() {
        val repository = (application as MessengerApplication).repository
        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]
        val sharedPref = (application as MessengerApplication).sharedReference
        loginViewModel.getLoggedInState(sharedPref)
        loginViewModel.loggedInAccount.observe(this){
            setupDrawerMenuItem(it)
        }
    }

    private fun setupDrawerMenuItem(account: Account?) {
        val menu = binding.navView.menu
        val itemLogin = menu.findItem(R.id.item_login)
        if(account !=null){
            itemLogin.setTitle(R.string.action_logout)
            itemLogin.setIcon(R.drawable.ic_logout)
        }else{
            itemLogin.setTitle(R.string.action_login)
            itemLogin.setIcon(R.drawable.ic_login)
        }
    }

    private fun setupDrawerLayoutMenuItemSelectedListener(){
        binding.navView.setNavigationItemSelectedListener {
            if(it.title.toString().compareTo(getString(R.string.action_login)) == 0){
                loginViewModel.updateLoginState(null)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }else if(it.title.toString().compareTo(getString(R.string.action_logout)) == 0){
                logout()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }else{
                false
            }
        }
    }

    private fun logout(){
        val sharedPreferences = (application as MessengerApplication).sharedReference
        loginViewModel.saveLoginState(sharedPreferences, false)
        loginViewModel.updateLoginState(null)
    }

}