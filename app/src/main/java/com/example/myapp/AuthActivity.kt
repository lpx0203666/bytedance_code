package com.example.myapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var dbHelper: UserDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    
    // Request code for starting Login Activity
    private val REQUEST_LOGIN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        dbHelper = UserDatabaseHelper(this)
        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        
        checkLoginAndShowUI()
    }
    
    override fun onResume() {
        super.onResume()
        // Optionally refresh UI in case login state changed outside (though onActivityResult handles it)
        // checkLoginAndShowUI() 
    }
    
    private fun checkLoginAndShowUI() {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        val currentUser = sharedPreferences.getString("current_user", "")
        
        if (!isLoggedIn || currentUser.isNullOrEmpty()) {
             showLoginRequiredUI()
        } else {
             showAuthUI(currentUser)
        }
    }
    
    private fun showLoginRequiredUI() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(50, 50, 50, 50)
        layout.setBackgroundColor(0xFFF5F5F5.toInt())
        
        val tvTitle = TextView(this)
        tvTitle.text = "需要登录"
        tvTitle.textSize = 24f
        tvTitle.setTextColor(0xFF000000.toInt())
        tvTitle.gravity = Gravity.CENTER
        layout.addView(tvTitle)
        
        val tvMessage = TextView(this)
        tvMessage.text = "App A 当前未登录。\n需要先登录 App A 才能授权快捷登录。"
        tvMessage.textSize = 16f
        tvMessage.setTextColor(0xFF888888.toInt())
        tvMessage.gravity = Gravity.CENTER
        tvMessage.setPadding(0, 30, 0, 50)
        layout.addView(tvMessage)
        
        val btnLogin = Button(this)
        btnLogin.text = "去登录"
        btnLogin.setOnClickListener {
            // Start MainActivity with a flag indicating it's an auth flow
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("IS_AUTH_FLOW", true)
            startActivityForResult(intent, REQUEST_LOGIN)
        }
        layout.addView(btnLogin)
        
        val btnCancel = Button(this)
        btnCancel.text = "取消"
        btnCancel.setBackgroundColor(0x00000000)
        btnCancel.setTextColor(0xFF888888.toInt())
        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        layout.addView(btnCancel)
        
        setContentView(layout)
    }
    
    private fun showAuthUI(currentUser: String) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(50, 50, 50, 50)
        layout.setBackgroundColor(0xFFF5F5F5.toInt())
        
        val tvTitle = TextView(this)
        tvTitle.text = "授权登录"
        tvTitle.textSize = 24f
        tvTitle.setTextColor(0xFF000000.toInt())
        tvTitle.gravity = Gravity.CENTER
        layout.addView(tvTitle)
        
        val tvMessage = TextView(this)
        tvMessage.text = "App B 申请获取您的账号信息"
        tvMessage.textSize = 16f
        tvMessage.setTextColor(0xFF888888.toInt())
        tvMessage.gravity = Gravity.CENTER
        tvMessage.setPadding(0, 30, 0, 50)
        layout.addView(tvMessage)
        
        val nickname = dbHelper.getUserNickname(currentUser)
        
        val tvUser = TextView(this)
        tvUser.text = "将使用当前登录账号:\n$nickname ($currentUser)"
        tvUser.textSize = 18f
        tvUser.setTextColor(0xFF2196F3.toInt())
        tvUser.gravity = Gravity.CENTER
        tvUser.setPadding(0, 0, 0, 60)
        layout.addView(tvUser)
        
        val btnAuth = Button(this)
        btnAuth.text = "确认授权"
        btnAuth.setBackgroundColor(0xFF2196F3.toInt())
        btnAuth.setTextColor(0xFFFFFFFF.toInt())
        btnAuth.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("AUTH_USERNAME", currentUser)
            resultIntent.putExtra("AUTH_NICKNAME", nickname)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        layout.addView(btnAuth)
        
        // Option to switch account or cancel
        val btnCancel = Button(this)
        btnCancel.text = "取消 / 切换账号"
        btnCancel.setBackgroundColor(0x00000000)
        btnCancel.setTextColor(0xFF888888.toInt())
        btnCancel.setOnClickListener {
            // Can redirect to Login to switch account
            // For now, just cancel
            setResult(RESULT_CANCELED)
            finish()
        }
        layout.addView(btnCancel)
        
        setContentView(layout)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                // Login successful, refresh UI to show auth screen
                checkLoginAndShowUI()
            } else {
                // User cancelled login
                // Stay on "Login Required" screen or finish? 
                // Let's stay so user can try again or click cancel.
            }
        }
    }
}
