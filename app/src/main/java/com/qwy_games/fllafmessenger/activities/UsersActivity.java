package com.qwy_games.fllafmessenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.qwy_games.fllafmessenger.adapters.UsersAdapter;
import com.qwy_games.fllafmessenger.databinding.ActivityUsersBinding;
import com.qwy_games.fllafmessenger.listeners.UserListener;
import com.qwy_games.fllafmessenger.models.User;
import com.qwy_games.fllafmessenger.utilities.Constants;
import com.qwy_games.fllafmessenger.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    // Отрисовка activity_users.xml и инициализация необходимых ресурсов
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Привязка layout к активности с помощью view binding
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализация PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        // Установка слушателей
        setListeners();

        // Получение данных всех пользователей
        getUser();
    }

    // Функция установки слушателей на компоненты активности
    private void setListeners() {
        // Устанавливает слушатель на 'назад', который возвращает пользователя на предыдущую активность
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    // Функция для получения списка всех пользователей
    private void getUser() {
        loading(true); // Отображение индикатора прогресса

        // Получим экземпляр Firestore и соберем пользователей из базы данных
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get().addOnCompleteListener(task -> {
                    loading(false); //Окончание отображения прогресса
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

                    // Если получение данных успешно завершено
                    if(task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            // Пропускаем текущего пользователя в списке
                            if(currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }

                            // Создаем объект User и заполняем его поля данными из Firestore
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();

                            // Добавляем пользователя в список
                            users.add(user);
                        }

                        // Если пользователи есть, выводим их в RecyclerView
                        if(users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            // Устанавливаем адаптер и делаем RecyclerView видимым
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            // Если пользователей нет, включаем сообщение об ошибке
                            showErrorMessage();
                        }
                    } else {
                        // Если получение данных завершилось неудачей, включаем сообщение об ошибке
                        showErrorMessage();
                    }
                });
    }

    // Функция для отображения сообщения об ошибке, когда нет доступных пользователей
    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE); // Делает видимым текстовое поле сообщения об ошибке
    }

    // Функция для управления отображением элементов интерфейса во время загрузки
    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE); // если true, показываем индикатор прогресса
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE); // если false, скрываем индикатор прогресса
        }
    }

    // Функция для перехода на страницу чата с выбранным пользователем, реализация метода из UserListener
    @Override
    public void onUserClicked(User user) {
        // Создаем намерение для перехода на ChatActivity
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        // Прикрепляем к Intent объект пользователя в качестве "extra"
        intent.putExtra(Constants.KEY_USER, user);
        // Начинаем активность
        startActivity(intent);
        finish(); // Завершаем текущую активность
    }
}