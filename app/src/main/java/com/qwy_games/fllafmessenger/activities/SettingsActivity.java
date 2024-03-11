package com.qwy_games.fllafmessenger.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.qwy_games.fllafmessenger.R;
import com.qwy_games.fllafmessenger.adapters.RecentConversationsAdapter;
import com.qwy_games.fllafmessenger.databinding.ActivityMainBinding;
import com.qwy_games.fllafmessenger.databinding.ActivitySettingsBinding;
import com.qwy_games.fllafmessenger.models.ChatMessage;
import com.qwy_games.fllafmessenger.utilities.Constants;
import com.qwy_games.fllafmessenger.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private ImageView profileImage;

    // Отрисовка activity_settings.xml и инициализация всего необходимого
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());

        init();
        loadUserDetails();
        getToken();
        setListeners();
    }

    // Инициализация базы данных
    private void init() {
        database = FirebaseFirestore.getInstance();
        profileImage = findViewById(R.id.imageProfile);
    }

    // Загрузка данных пользователя
    private void loadUserDetails() {
        binding.settingsTextName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.settingsImageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Фунция для получения токена FirebaseMessaging (fcmToken)
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    // Функция для кнопок, чтобы они работали
    private void setListeners() {
       binding.imageBackToMain.setOnClickListener(v -> {
           Intent intent = new Intent(getApplicationContext(), MainActivity.class);
           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
           startActivity(intent);
       });
    }

    // Фунция для обновления токена FirebaseMessaging (fcmToken)
    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }
}