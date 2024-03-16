package com.qwy_games.fllafmessenger.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.qwy_games.fllafmessenger.R;
import com.qwy_games.fllafmessenger.databinding.ActivitySettingsBinding;
import com.qwy_games.fllafmessenger.utilities.Constants;
import com.qwy_games.fllafmessenger.utilities.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding; // Объект для работы с элементами интерфейса вьюхи на основе ViewBinding
    private FirebaseFirestore database; // Объект Firebase Firestore для работы с базой данных
    private PreferenceManager preferenceManager; // Объект для работы с хранилищем настроек
    private ImageView profileImage; // Объект ImageView для отображения изображения профиля

    // Отрисовка activity_settings.xml и инициализация всех необходимых объектов
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater()); // Инициализация ViewBinding
        preferenceManager = new PreferenceManager(getApplicationContext()); // Инициализация менеджера настроек
        setContentView(binding.getRoot()); // Установка вьюхи в качестве корневой для данной активности

        init(); // Вызов метода для инициализации объектов базы данных и изображения профиля
        loadUserDetails(); // Загрузка и отображение данных пользователя
        getToken(); // Получение токена устройства Firebase Cloud Message
        setListeners(); // Вызов метода для установки слушателей на вьюхи
    }

    // Функция инициализации объектов базы данных и изображения профиля
    private void init() {
        database = FirebaseFirestore.getInstance(); // Инициализация экземпляра Firestore
        profileImage = findViewById(R.id.imageProfile); // Инициализация объекта ImageView
    }

    // Функция для загрузки и отображения данных пользователя из сохраненных настроек
    private void loadUserDetails() {
        binding.settingsTextName.setText(preferenceManager.getString(Constants.KEY_NAME)); // Отображение имени пользователя
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT); // Декодирование Base64 изображения профиля
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length); // Преобразование байтов в Bitmap
        binding.settingsImageProfile.setImageBitmap(bitmap); // Установка изображения профиля в ImageView
    }

    // Функция для отображения toast сообщений пользователю
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Функция для получения FCM токена устройства
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken); // Получение текущего токена и отправка его на обновление
    }

    // Установка слушателей на вьюхи
    private void setListeners() {
        // Установка слушателя нажатия на кнопку 'назад', который перенаправит пользователя обратно на MainActivity
        binding.imageBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // Функция для обновления FCM токена устройства в Firestore
    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token); // Сохранение токена в настройках
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token) // Обновление FCM токена для данного пользователя в Firestore
                .addOnFailureListener(e -> showToast("Unable to update token")); // Вывод сообщения в случае неудачи
    }
}