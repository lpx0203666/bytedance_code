package com.example.myapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnWechat: LinearLayout
    private lateinit var btnApple: LinearLayout
    private lateinit var tvRegister: TextView
    private lateinit var dbHelper: UserDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    // 标志位：是否是授权登录流程
    private var isAuthFlow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查 Intent 中是否有授权流程的标志
        isAuthFlow = intent.getBooleanExtra("IS_AUTH_FLOW", false)
        
        dbHelper = UserDatabaseHelper(this)
        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // 如果不是授权流程，且已经登录，直接跳转到个人中心
        if (!isAuthFlow && isUserLoggedIn()) {
            val username = sharedPreferences.getString("current_user", "")
            val intent = Intent(this, PersonalCenterActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        initViews()
        initDatabase()
        resizeInputIcons()
        initListeners()
        checkAutoLogin()
    }
    
    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnWechat = findViewById(R.id.btnWechat)
        btnApple = findViewById(R.id.btnApple)
        tvRegister = findViewById(R.id.tvRegister)
    }

    private fun initDatabase() {
        if (!dbHelper.isUserExists("admin")) {
            dbHelper.addUser("admin", "123456")
        }
        
        if (!sharedPreferences.contains("user_signature")) {
            val editor = sharedPreferences.edit()
            editor.putString("user_signature", "这里是个性签名，欢迎来到我的App")
            editor.apply()
        }
    }
    
    private fun checkAutoLogin() {
        val lastUser = sharedPreferences.getString("last_username", "")
        if (!lastUser.isNullOrEmpty()) {
            etUsername.setText(lastUser)
        }
    }

    private fun resizeInputIcons() {
        val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size_small)
        resizeCompoundDrawables(etUsername, iconSize)
        resizeCompoundDrawables(etPassword, iconSize)
    }

    private fun resizeCompoundDrawables(textView: TextView, size: Int) {
        val drawables = textView.compoundDrawablesRelative
        val start = drawables[0] 
        
        if (start != null) {
            start.setBounds(0, 0, size, size)
            textView.setCompoundDrawablesRelative(start, null, null, null)
        }
    }

    private fun initListeners() {
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.checkUser(username, password)) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                
                // 保存登录状态
                sharedPreferences.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("current_user", username)
                    .putString("last_username", username)
                    .apply()
                
                // 更新数据库最后登录时间
                dbHelper.updateLastLogin(username)

                if (isAuthFlow) {
                    // 如果是授权流程，登录成功后直接 finish，返回给 AuthActivity
                    // 不需要跳转到个人中心
                    setResult(RESULT_OK)
                    finish()
                } else {
                    // 正常流程，跳转到个人中心
                    val intent = Intent(this, PersonalCenterActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show()
            }
        }

        btnWechat.setOnClickListener {
            Toast.makeText(this, "微信登录被点击", Toast.LENGTH_SHORT).show()
        }

        btnApple.setOnClickListener {
            Toast.makeText(this, "Apple登录被点击", Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            showRegisterDialog()
        }
    }
    
    private fun showRegisterDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("注册账号")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputUsername = EditText(this)
        inputUsername.hint = "请输入用户名"
        layout.addView(inputUsername)

        val inputPassword = EditText(this)
        inputPassword.hint = "请输入密码"
        inputPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(inputPassword)

        builder.setView(layout)

        builder.setPositiveButton("注册") { dialog, which ->
            val username = inputUsername.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                if (dbHelper.isUserExists(username)) {
                    Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show()
                } else {
                    dbHelper.addUser(username, password)
                    Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                    etUsername.setText(username)
                    etPassword.setText(password)
                }
            } else {
                Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("取消") { dialog, which -> dialog.cancel() }

        builder.show()
    }
}
