package com.example.myapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PersonalCenterActivity extends AppCompatActivity {

    private TextView tvUsername;
    private TextView tvSignature;
    private ImageView ivAvatar; 
    private TextView itemPersonalInfo;
    private TextView itemFavorites;
    private TextView itemHistory;
    private TextView itemSettings;
    private TextView itemAbout;
    private TextView itemFeedback;
    private Button btnLogout; 
    private ImageView btnAccountManager;

    private UserDatabaseHelper dbHelper;
    private String currentUsername;
    
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_center);
        
        dbHelper = new UserDatabaseHelper(this);

        initViews();
        loadData();
        initListeners();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvSignature = findViewById(R.id.tvSignature);
        ivAvatar = findViewById(R.id.ivAvatar); 
        itemPersonalInfo = findViewById(R.id.itemPersonalInfo);
        itemFavorites = findViewById(R.id.itemFavorites);
        itemHistory = findViewById(R.id.itemHistory);
        itemSettings = findViewById(R.id.itemSettings);
        itemAbout = findViewById(R.id.itemAbout);
        itemFeedback = findViewById(R.id.itemFeedback);
        btnLogout = findViewById(R.id.btnLogout); 
        btnAccountManager = findViewById(R.id.btnAccountManager);
    }

    private void loadData() {
        currentUsername = getIntent().getStringExtra("USERNAME");
        
        if (currentUsername == null || currentUsername.isEmpty()) {
            SharedPreferences sp = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            currentUsername = sp.getString("current_user", "");
        }

        if (currentUsername != null && !currentUsername.isEmpty()) {
            String nickname = dbHelper.getUserNickname(currentUsername);
            tvUsername.setText(nickname);
            
            // Load Avatar
            String avatarUriStr = dbHelper.getUserAvatar(currentUsername);
            if (avatarUriStr != null && !avatarUriStr.isEmpty()) {
                if (avatarUriStr.startsWith("android.resource")) {
                    // System default avatar
                    try {
                        ivAvatar.setImageResource(Integer.parseInt(avatarUriStr.split("/")[1])); // Just a hacky way, better store resId
                    } catch (Exception e) {
                         // If stored as uri string
                         ivAvatar.setImageURI(Uri.parse(avatarUriStr));
                    }
                } else {
                    // Gallery uri
                    try {
                        ivAvatar.setImageURI(Uri.parse(avatarUriStr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            tvUsername.setText("未登录用户");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String signature = sharedPreferences.getString("user_signature", "这里是个性签名，欢迎来到我的App");
        tvSignature.setText(signature);
    }

    private void initListeners() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.itemPersonalInfo) {
                    showEditProfileDialog();
                } else if (v instanceof TextView) {
                    CharSequence text = ((TextView) v).getText();
                    Toast.makeText(PersonalCenterActivity.this, "点击了: " + text, Toast.LENGTH_SHORT).show();
                }
            }
        };

        itemPersonalInfo.setOnClickListener(listener);
        itemFavorites.setOnClickListener(listener);
        itemHistory.setOnClickListener(listener);
        itemSettings.setOnClickListener(listener); 
        itemAbout.setOnClickListener(listener);
        itemFeedback.setOnClickListener(listener);
        
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        findViewById(R.id.cardProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 showEditProfileDialog();
            }
        });
        
        ivAvatar.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showAvatarSelectionDialog();
             }
        });
        
        btnAccountManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalCenterActivity.this, AccountManagerActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void showAvatarSelectionDialog() {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_select_avatar, null);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Option 1: Pick from Gallery
        dialogView.findViewById(R.id.btnPickFromGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                dialog.dismiss();
            }
        });

        // Option 2: Default Avatars
        View.OnClickListener defaultAvatarListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resId = 0;
                if (v.getId() == R.id.ivDef1) {
                    resId = android.R.drawable.sym_def_app_icon;
                } else if (v.getId() == R.id.ivDef2) {
                    resId = android.R.drawable.star_big_on;
                } else if (v.getId() == R.id.ivDef3) {
                    resId = android.R.drawable.ic_menu_camera;
                } else if (v.getId() == R.id.ivDef4) {
                    resId = android.R.drawable.ic_menu_myplaces;
                } else if (v.getId() == R.id.ivDef5) {
                    resId = android.R.drawable.ic_menu_call;
                }
                
                if (resId != 0) {
                    updateAvatarResource(resId);
                    dialog.dismiss();
                }
            }
        };

        dialogView.findViewById(R.id.ivDef1).setOnClickListener(defaultAvatarListener);
        dialogView.findViewById(R.id.ivDef2).setOnClickListener(defaultAvatarListener);
        dialogView.findViewById(R.id.ivDef3).setOnClickListener(defaultAvatarListener);
        dialogView.findViewById(R.id.ivDef4).setOnClickListener(defaultAvatarListener);
        dialogView.findViewById(R.id.ivDef5).setOnClickListener(defaultAvatarListener);

        dialog.show();
    }
    
    private void updateAvatarResource(int resId) {
        if (currentUsername != null && !currentUsername.isEmpty()) {
            Uri resUri = Uri.parse("android.resource://" + getPackageName() + "/" + resId);
            dbHelper.updateUserAvatar(currentUsername, resUri.toString());
            ivAvatar.setImageURI(resUri);
            
            // Fallback for visual update if Uri fails for internal resources
            ivAvatar.setImageResource(resId);
            
            Toast.makeText(this, "头像已更新", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (currentUsername != null && !currentUsername.isEmpty()) {
                dbHelper.updateUserAvatar(currentUsername, imageUri.toString());
                ivAvatar.setImageURI(imageUri);
                Toast.makeText(this, "头像已更新", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("编辑个人资料");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        Button btnChangeAvatar = new Button(this);
        btnChangeAvatar.setText("更换头像");
        btnChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvatarSelectionDialog(); // Changed to use the selection dialog
            }
        });
        layout.addView(btnChangeAvatar);

        final EditText inputNickname = new EditText(this);
        inputNickname.setHint("新昵称");
        inputNickname.setText(tvUsername.getText());
        layout.addView(inputNickname);

        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("新密码 (留空不修改)");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        builder.setView(layout);

        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newNickname = inputNickname.getText().toString().trim();
                String newPassword = inputPassword.getText().toString().trim();
                
                if (!newNickname.isEmpty()) {
                    dbHelper.updateUserNickname(currentUsername, newNickname);
                    tvUsername.setText(newNickname);
                }
                
                if (!newPassword.isEmpty()) {
                    dbHelper.updateUserPassword(currentUsername, newPassword);
                    Toast.makeText(PersonalCenterActivity.this, "密码已修改，请重新登录", Toast.LENGTH_SHORT).show();
                    logout();
                } else {
                    Toast.makeText(PersonalCenterActivity.this, "资料已更新", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logout();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void logout() {
        SharedPreferences sp = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("is_logged_in");
        editor.remove("current_user");
        editor.apply();
        
        Intent intent = new Intent(PersonalCenterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
