package com.qwy_games.fllafmessenger.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.qwy_games.fllafmessenger.databinding.ActivitySignInBinding;
import com.qwy_games.fllafmessenger.utilities.Constants;
import com.qwy_games.fllafmessenger.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    // Отображение activity_sign_in.xml и инициализация необходимых ресурсов
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализируем менеджер настроек
        preferenceManager = new PreferenceManager(getApplicationContext());

        // Проверяем, выполнен ли вход в систему, если да, то переходим на MainActivity
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish(); // Завершаем текущую активность
        }

        // Привязка layout к активности через ViewBinding
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Установка слушателей событий
        setListeners();
    }

    // Функция для настройки функциональности кнопок
    private void setListeners() {
        // Нажатие на текст "create new account" перенаправляет пользователя на страницу регистрации
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

        // Нажатие на кнопку входа сначала проверяет валидность введенных данных, затем входит в систему
        binding.buttonSignIn.setOnClickListener(v -> {
            if(isValidSignInDetails()) {
                signIn();
            }
        });
    }

    // Логика входа в учетную запись
    private void signIn() {
        loading(true); // Отображение прогресса загрузки
        FirebaseFirestore database = FirebaseFirestore.getInstance(); // Получение экземпляра Firestore
        // Запрашиваем пользовательскую коллекцию по введенным email и паролю
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get().addOnCompleteListener(task -> {
                    // Если успешно получены данные и они не пустые
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        // Обновляем сведения о входе в систему в наших настройках
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        // Сохраняем детали пользователя в настройках
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        // Переходим на MainActivity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        // В случае неудачи, прекращаем отображение индикатора загрузки и сообщаем о проблеме
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });
    }

    // Функция для отображения текстовых уведомлений (toast messages)
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Проверка правильности ввода деталей входа в систему
    private Boolean isValidSignInDetails() {
        // Если поле электронной почты пустое, выводим сообщение об ошибке
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        }
        // Если введенная электронная почта недействительна, выводим сообщение об ошибке
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        }
        // Если поле пароля пустое, выводим сообщение об ошибке
        else if(binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        }
        // Если все поля валидны, возвращаем true
        else {
            return true;
        }
    }

    // Функция для отображения индикатора загрузки во время входа в систему
    private void loading(Boolean isLoading) {
        if(isLoading) { // Если процесс загрузки активен, скрываем кнопку и показываем индикатор загрузки
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else { // Если загрузка завершена, скрываем индикатор загрузки и показываем кнопку
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }
}