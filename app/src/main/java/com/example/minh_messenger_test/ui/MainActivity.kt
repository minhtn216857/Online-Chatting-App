package com.example.minh_messenger_test.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.minh_messenger_test.MessengerApplication
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Account
import com.example.minh_messenger_test.data.model.AccountStatus
import com.example.minh_messenger_test.databinding.ActivityMainBinding
import com.example.minh_messenger_test.service.MainService
import com.example.minh_messenger_test.service.MainServiceActions
import com.example.minh_messenger_test.service.MainServiceRepository
import com.example.minh_messenger_test.ui.login.LoginViewModel
import com.example.minh_messenger_test.ui.login.LoginViewModelFactory
import com.example.minh_messenger_test.ui.voicecall.VoiceCallActivity
import com.example.minh_messenger_test.utils.DataModel
import com.example.minh_messenger_test.utils.DataModelType
import com.example.minh_messenger_test.utils.MessengerUtils
import com.example.minh_messenger_test.utils.getCameraAndMicPermission
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainService.Listener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var loginViewModel: LoginViewModel
    @Inject lateinit var databaseRef: DatabaseReference
    @Inject lateinit var mainServiceRepository: MainServiceRepository

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

    private fun startMyService(username: String) {
//        val username = LoginViewModel.currentAccount.value!!.username
        Log.d("started", "started")

        mainServiceRepository.startService(username)
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
                R.id.call_fragment, // ID của Voice Call Fragment
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
                || destination.id == R.id.call_fragment
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
            val shouldShowPrompt =
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)

            if (selPms == PackageManager.PERMISSION_GRANTED) {
                postNotification()
            }
            else if (shouldShowPrompt) {
                showMessage(R.string.mesg_permission_prompt, Snackbar.LENGTH_LONG)
            }
            else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun retrieveToken() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                MessengerUtils.token = task.result // Lưu token vào biến toàn cục
                Log.d("FCM", "Token initialized: ${MessengerUtils.token}")
            } else {
                Log.e("FCM", "Failed to fetch token", task.exception)
            }
        }
    }

    private fun setupViewModel() {
        MainService.listener = this

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
        loginViewModel.loginState.observe(this) {
            if (!it.status || it.username == null) {
                Log.d("Login", "Failuare Login")
            } else {
                Log.d("username", "${it.username}")
                // load thong tin tai khoan da dang nhap tu local
                startMyService(it.username)
            }
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

    private fun setupDrawerLayoutMenuItemSelectedListener() {
        binding.navView.setNavigationItemSelectedListener {
            if (it.title.toString().compareTo(getString(R.string.action_login)) == 0) {
                loginViewModel.updateLoginState(null)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            if (it.title.toString().compareTo(getString(R.string.action_logout)) == 0) {
                logout()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            if(it.itemId == R.id.item_add_friend){
                if(navController.currentDestination?.id == R.id.call_fragment){
                    navController.navigate(R.id.action_call_fragment_to_addFriendFragment)
                }else{
                    navController.navigate(R.id.action_home_fragment_to_addFriendFragment)
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            if(it.itemId == R.id.item_profile){
                if(navController.currentDestination?.id == R.id.call_fragment){
                    navController.navigate(R.id.action_call_fragment_to_profileFragment)
                }else{
                    navController.navigate(R.id.action_home_fragment_to_profileFragment)

                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            else {
                false
            }

        }
    }

    private fun logout(){
        val sharedPreferences = (application as MessengerApplication).sharedReference
        val username = LoginViewModel.currentAccount.value!!.username
        updateStatusLogoutRealtimeDatabase(username)
        loginViewModel.saveLoginState(sharedPreferences, false)
        loginViewModel.updateLoginState(null)

        // Dừng service khi đăng xuất
        val stopServiceIntent = Intent(this, MainService::class.java).apply {
            action = MainServiceActions.STOP_SERVICE.name
        }
        stopService(stopServiceIntent)


    }

    private fun updateStatusLogoutRealtimeDatabase(username: String){
        val databaseRef = Firebase.database.reference
        databaseRef.child(username).child("status").setValue(AccountStatus.OFFLINE)
    }

    @SuppressLint("SetTextI18n")
    override fun onCallReceived(model: DataModel) {
        runOnUiThread {
            binding.includeMain.incomingCallLayout.apply {
                val isVideoCall = model.type == DataModelType.StartVideoCall
                val isVideoCallText = if(isVideoCall) "Video" else "Audio"
                txtIncomingCall.text = "${model.sender} is ${isVideoCallText} Calling you"
                incomingCallLayout.isVisible = true
                btnYesCall.setOnClickListener {
                    getCameraAndMicPermission {
                        incomingCallLayout.isVisible = false
                        startActivity(
                            Intent(this@MainActivity,
                                VoiceCallActivity::class.java).apply {
                                putExtra("target", model.sender)
                                putExtra("isVideoCall",isVideoCall)
                                putExtra("isCaller", false)
                            })
                    }
                }
                btnNoCall.setOnClickListener {
                    incomingCallLayout.isVisible = false
                }
            }

        }
    }

}