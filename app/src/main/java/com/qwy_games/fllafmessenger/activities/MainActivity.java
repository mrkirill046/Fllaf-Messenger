package com.qwy_games.fllafmessenger.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.qwy_games.fllafmessenger.R;
import com.qwy_games.fllafmessenger.adapters.RecentConversationsAdapter;
import com.qwy_games.fllafmessenger.databinding.ActivityMainBinding;
import com.qwy_games.fllafmessenger.listeners.ConversionListener;
import com.qwy_games.fllafmessenger.models.ChatMessage;
import com.qwy_games.fllafmessenger.models.User;
import com.qwy_games.fllafmessenger.utilities.Constants;
import com.qwy_games.fllafmessenger.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding; // Инициализация объекта View Binding
    private PreferenceManager preferenceManager; // Инициализация объекта для работы с Shared Preferences
    private List<ChatMessage> conversations; // Список для хранения списка пользовательских бесед
    private RecentConversationsAdapter conversationsAdapter; // Адаптер для связывания данных бесед с RecyclerView
    private FirebaseFirestore database; // Представление Firestore database
    private NavigationView navigationView; // Боковая навигационная панель
    private DrawerLayout drawerLayout; // Корневой макет, содержащий содержимое и боковую панель
    private ImageView profileImage; // ImageView для отображения изображения профиля пользователя

    // Отображение activity_main.xml и инициализация необходимых ресурсов
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); // Прикрепить к активности макет activity_main с помощью View Binding
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext()); // Инициализация объекта preferenceManager
        drawerLayout = findViewById(R.id.drawer_layout); // Инициализация drawerLayout

        init(); // Инициализация основных компонентов и данных
        loadUserDetails(); // Загрузка деталей пользователя из Shared Preferences
        getToken(); // Получение Firebase Messaging Token
        setListeners(); // Установка обработчиков для различных элементов управления на UI
        listenConversations(); // Настройка слушателя для обновления бесед в реальном времени
    }

    // Инициализация адаптеров, conversation, базы данных, бокового меню
    private void init() {
        conversations = new ArrayList<>(); // Инициализация списка для хранения бесед
        conversationsAdapter = new RecentConversationsAdapter(conversations, this); // Инициализация адаптера
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter); // Связывание адаптера и RecyclerView
        database = FirebaseFirestore.getInstance(); // Получение экземпляра Firebase Firestore
        profileImage = findViewById(R.id.imageProfile); // Инициализация ImageView для изображения профиля пользователя

        navigationView = findViewById(R.id.navigation_view); // Инициализация боковой навигационной панели
        // Установка слушателя для элементов меню в боковой панели
        navigationView.setNavigationItemSelectedListener(item -> {
            // Обработка нажатий на элементы бокового меню
            // ...
            return false;
        });
    }

    // Установка слушателей событий кликов по кнопкам
    private void setListeners() {
        // Кнопка "Выход"
        binding.imageSignOut.setOnClickListener(v -> signOut());
        // Кнопка "Новый чат"
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
        // Кнопка профиля
        binding.imageProfile.setOnClickListener(v -> drawerLayout.open());
    }

    // Загрузка данных пользователя в UI
    private void loadUserDetails() {
        // Установка имени пользователя
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        // Преобразование закодированного изображения профиля обратно в битовую карту и установка его в ImageView
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    // Функция для отображения краткосрочных всплывающих сообщений (toast messages)
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Функция для прослушивания обновлений в беседах пользователя
    private void listenConversations() {
        // Добавление слушателя к документам, в которых текущий пользователь является отправителем
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        // Добавление слушателя к документам, в которых текущий пользователь является получателем
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    // Обработчик событий для обнаружения добавления или изменения документов в коллекции
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        // Если при обработке произошла ошибка, возвращаемся из обработчика
        if(error != null) {
            return;
        }
        // Если данные успешно получены
        if(value != null) {
            // Цикл по всем изменениям в документах
            for(DocumentChange documentChange : value.getDocumentChanges()) {
                // Если документ был добавлен
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    // В зависимости от того, текущий пользователь является отправителем или получателем, устанавливаем соответствующие данные
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    // Добавляем новую беседу в список
                    conversations.add(chatMessage);
                } else if(documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    // Если документ был изменен, обновляем соответствующую беседу в списке
                    for(int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            // Сортируем беседы по дате в порядке убывания
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            // Скроллим к последнему сообщению
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            // Показываем RecyclerView и скрываем ProgressBar
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    // получение FirebaseMessaging токена для текущего устройства
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        // Сохранение полученного токена в SharedPreferences
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        // Обновление токена устройства в текущем профиле пользователя в Firestore
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    // Выход из учетной записи и переход на экран входа в систему
    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        // Удаление токена устройства из профиля пользователя в Firestore
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        // Обновление Firestore документа
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    // Очистка всей информации, хранится в SharedPreferences
                    preferenceManager.clear();
                    // Переход на экран входа в систему
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    // Завершение текущего активити
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out")); // Если не удалось выйти из системы, показать сообщение об ошибке
    }

    @Override
    public void onConversionListener(User user) {
        // Обработчик, который вызывается, когда пользователь нажимает на одну из бесед
        // Создание Intent для перехода к активности Chat
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        // Добавление информации о выбранном пользователе в Intent как Extra Data
        intent.putExtra(Constants.KEY_USER, user);
        // Запуск активности Chat, передавая Intent
        startActivity(intent);
    }
}