package com.example.appb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appb.ui.theme.MyAppTheme

class appbActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val username = data?.getStringExtra("AUTH_USERNAME") ?: "未知"
            val nickname = data?.getStringExtra("AUTH_NICKNAME") ?: "未知"
            
            Toast.makeText(context, "授权成功", Toast.LENGTH_SHORT).show()
            
            // 跳转到欢迎界面 Activity
            val intent = Intent(context, WelcomeActivity::class.java).apply {
                putExtra("AUTH_USERNAME", username)
                putExtra("AUTH_NICKNAME", nickname)
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "用户取消了授权", Toast.LENGTH_SHORT).show()
        }
    }

    // 登录前的界面
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "App B",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "快捷登录示例",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = {
                val intent = Intent("com.example.myapp.ACTION_AUTH_LOGIN")
                try {
                    launcher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "未找到 App A，请先安装", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("使用 App A 快捷登录", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyAppTheme {
        LoginScreen()
    }
}
