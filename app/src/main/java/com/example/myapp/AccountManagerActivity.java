package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AccountManagerActivity extends AppCompatActivity {

    private LinearLayout llAccountList;
    private Button btnAddAccount;
    
    // Current user UI
    private TextView tvCurrentNickname;
    private TextView tvCurrentUsername;
    private ImageView ivCurrentAvatar;

    private UserDatabaseHelper dbHelper;
    private String currentUsername;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);

        dbHelper = new UserDatabaseHelper(this);
        
        // Get current user
        SharedPreferences sp = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        currentUsername = sp.getString("current_user", "");

        initViews();
        loadCurrentUserInfo();
        loadAccountList();
        
        btnAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add new account means going to Login screen (MainActivity)
                // We should clear login state but NOT current_user immediately, or handle it in MainActivity
                // Ideally, we start MainActivity with a flag to clear fields
                Intent intent = new Intent(AccountManagerActivity.this, MainActivity.class);
                intent.putExtra("ADD_ACCOUNT", true);
                startActivity(intent);
                // We don't finish this activity yet, or we could.
                // Actually, if we add account, we probably want to log in as that new user.
                // So logging out current session is implied for "Adding Account" flow usually, 
                // or we just switch to login screen.
            }
        });
    }

    private void initViews() {
        llAccountList = findViewById(R.id.llAccountList);
        btnAddAccount = findViewById(R.id.btnAddAccount);
        tvCurrentNickname = findViewById(R.id.tvCurrentNickname);
        tvCurrentUsername = findViewById(R.id.tvCurrentUsername);
        ivCurrentAvatar = findViewById(R.id.ivCurrentAvatar);
    }
    
    private void loadCurrentUserInfo() {
        if (!currentUsername.isEmpty()) {
            tvCurrentUsername.setText(currentUsername);
            String nickname = dbHelper.getUserNickname(currentUsername);
            tvCurrentNickname.setText(nickname);
            
            String avatarUriStr = dbHelper.getUserAvatar(currentUsername);
            setAvatar(ivCurrentAvatar, avatarUriStr);
            
            // Update last login time now or when they actually logged in? 
            // Better to update when they actually switch or login.
        } else {
             // Should not happen if accessed from Personal Center
             tvCurrentNickname.setText("未登录");
        }
    }

    private void loadAccountList() {
        llAccountList.removeAllViews();
        List<UserDatabaseHelper.UserInfo> users = dbHelper.getAllUsers();
        
        for (final UserDatabaseHelper.UserInfo user : users) {
            // Skip current user in the switch list
            if (user.getUsername().equals(currentUsername)) continue;
            
            View itemView = createAccountItemView(user);
            llAccountList.addView(itemView);
        }
    }
    
    private View createAccountItemView(final UserDatabaseHelper.UserInfo user) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(40, 30, 40, 30);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
        
        // Avatar
        ImageView ivAvatar = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
        ivAvatar.setLayoutParams(params);
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // Simple circle mask or shape could be applied here
        ivAvatar.setBackgroundResource(R.drawable.circle_shape); 
        ivAvatar.setPadding(2,2,2,2);
        setAvatar(ivAvatar, user.getAvatarUri());
        layout.addView(ivAvatar);
        
        // Text Info
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        textParams.setMargins(40, 0, 0, 0);
        textLayout.setLayoutParams(textParams);
        
        TextView tvNickname = new TextView(this);
        tvNickname.setText(user.getNickname());
        tvNickname.setTextSize(16);
        tvNickname.setTextColor(Color.BLACK);
        tvNickname.getPaint().setFakeBoldText(true);
        textLayout.addView(tvNickname);
        
        TextView tvUsername = new TextView(this);
        tvUsername.setText(user.getUsername());
        tvUsername.setTextSize(14);
        tvUsername.setTextColor(Color.GRAY);
        textLayout.addView(tvUsername);
        
        layout.addView(textLayout);
        
        // Switch Button
        Button btnSwitch = new Button(this);
        btnSwitch.setText("切换");
        btnSwitch.setTextSize(12);
        btnSwitch.setTextColor(Color.parseColor("#2196F3"));
        btnSwitch.setBackgroundColor(Color.TRANSPARENT);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchAccount(user.getUsername());
            }
        });
        layout.addView(btnSwitch);
        
        // Separator line logic could be added here
        
        return layout;
    }
    
    private void setAvatar(ImageView imageView, String avatarUriStr) {
        if (avatarUriStr != null && !avatarUriStr.isEmpty()) {
            try {
                 if (avatarUriStr.startsWith("android.resource")) {
                    try {
                        imageView.setImageResource(Integer.parseInt(avatarUriStr.split("/")[1])); 
                    } catch (Exception e) {
                         imageView.setImageURI(Uri.parse(avatarUriStr));
                    }
                } else {
                    imageView.setImageURI(Uri.parse(avatarUriStr));
                }
            } catch (Exception e) {
                imageView.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        } else {
            imageView.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }
    
    private void switchAccount(String targetUsername) {
        // Update login state
        SharedPreferences sp = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        sp.edit()
          .putBoolean("is_logged_in", true)
          .putString("current_user", targetUsername)
          .putString("last_username", targetUsername)
          .apply();
          
        // Update last login time
        dbHelper.updateLastLogin(targetUsername);
          
        Toast.makeText(this, "已切换到 " + targetUsername, Toast.LENGTH_SHORT).show();
        
        // Go to Personal Center directly, clearing back stack to avoid confusion
        Intent intent = new Intent(AccountManagerActivity.this, PersonalCenterActivity.class);
        intent.putExtra("USERNAME", targetUsername);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
