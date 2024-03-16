package com.qwy_games.fllafmessenger.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.qwy_games.fllafmessenger.databinding.ActivitySignUpBinding;
import com.qwy_games.fllafmessenger.utilities.Constants;
import com.qwy_games.fllafmessenger.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage; // переменная для хранения зашифрованного изображения профиля
    private boolean isValidEmail; // флаг доступности e-mail
    private boolean isValidName; // флаг доступности имени

    // Отрисовка activity_sign_up.xml и инициализация всего необходимого
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // инициализация Firebase в этом активити
        binding = ActivitySignUpBinding.inflate(getLayoutInflater()); // привязка layout с помощью ViewBinding
        setContentView(binding.getRoot()); // установка привязанного layout на экран
        preferenceManager = new PreferenceManager(getApplicationContext()); // инициализация менеджера настроек
        setListeners(); // вызов функции установки обработчиков событий
    }

    // Функция для установки обработчиков событий
    private void setListeners() {
        // установка возвращения на предыдущую активити по нажатию
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        // обработчик нажатия на кнопку регистрации
        binding.buttonSignUp.setOnClickListener(v -> {
            binding.textError.setText("");

            // запускаем проверку e-mail и имени пользователя на уникальность
            isValidEmail();
            isValidName();

            // создаем задержку, чтобы дать методам isValidEmail и isValidName время выполниться
            new Handler().postDelayed(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    // проверяем валидность введенных данных, если все верно, выполняем signUp
                    if (isValidSignUpDetails() && isValidName && isValidEmail) {
                        binding.textSuccessful.setText("That's right, welcome to our messenger!");
                        binding.textError.setText("");
                        signUp();
                    }
                }
            }, 1500);
        });

        // установка выбора изображения по нажатию на layoutImage
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    // Функция для отображения коротких текстовых сообщений (Toast)
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Функция для регистрирования новой учетной записи
    private void signUp() {
        loading(true); // отображение индикатора загрузки на время регистрации

        FirebaseFirestore database = FirebaseFirestore.getInstance(); // получение экземпляра базы данных
        HashMap<String, Object> user = new HashMap<>(); // создание hashMap для хранения данных пользователя
        // Заполнение HashMap user данными, введенными пользователем
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS) // ссылка на коллекцию пользователей
                .add(user) // добавление нового пользователя
                .addOnSuccessListener(documentReference -> { // обработка успешной регистрации
                    loading(false); // закрытие индикатора загрузки
                    String userName = binding.inputName.getText().toString();

                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, userName);
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    // переход на основное активити после успешной регистрации
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> { // обработка неудачной регистрации
                    showToast(exception.getMessage()); // показ сообщения об ошибке
                });
    }

    // Функция для шифрования аватарки пользователя
    private String encodeImage(Bitmap bitmap) {
        // Устанавливаем ширину и высоту миниатюры изображения
        int previewWidth = 150;
        int previewHeight = 150;

        // Сжатие изображения на основе заданной ширины и высоты
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);

        // Используя ByteArrayOutputStream, преобразуем Bitmap в байтовый массив
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        // Закодировать байтовый массив в строку Base64 и возвратить
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //Перехватчик результата выбора изображения из галереи
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            //Открываем поток для чтения данных из Uri источника и декодируем в Bitmap
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap); //Отображаем выбранное изображение в ImageView
                            binding.textAddImage.setVisibility(View.GONE); //Скрываем текстовую подсказку
                            encodedImage = encodeImage(bitmap); // Закодировать выбранное изображение
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    // Интерфейс Optional callback, используемый для получения результатов проверки электронной почты
    interface EmailExistCallBack {
        void onCompleted(boolean isExist);
    }

    // Метод проверки существования электронного адреса
    @SuppressLint("SetTextI18n")
    private void checkIfEmailExists(String email, EmailExistCallBack callBack) {
        FirebaseFirestore database = FirebaseFirestore.getInstance(); // Получаем экземпляр Firebase Firestore
        CollectionReference usersRef = database.collection(Constants.KEY_COLLECTION_USERS); // ссылка на коллекцию пользователей
        usersRef.whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> { // Запрос к Firebase Firestore, чтобы проверить, существует ли такой email
                    if (task.isSuccessful()) {
                        boolean isExist = false;
                        for (QueryDocumentSnapshot document : task.getResult()) { // Навигация по результатам запроса
                            if (document.exists()){
                                isExist = true; // Устанавливаем флаг, если найден соответствующий документ
                                binding.textError.setText("The user with this email has already been registered"); // Выводим сообщение об ошибке
                                break;
                            }
                        }
                        callBack.onCompleted(isExist); // вызываем callback с результатом проверки
                    } else {
                        showToast("Error"); // Иначе выводим сообщение об ошибке
                    }
                });
    }

    // Функция вызывающая проверку электронного адреса
    private void isValidEmail() {
        checkIfEmailExists(binding.inputEmail.getText().toString().trim(), isExist -> {
            isValidEmail = !isExist;  // Обновление значения isValidEmail
        });
    }

    // Интерфейс Optional callback, используемый для получения результатов проверки имени
    interface NameExistCallBack {
        void onCompleted(boolean isExist);
    }

    // Метод проверки существования имени пользователя
    @SuppressLint("SetTextI18n")
    private void checkIfNameExists(String name, NameExistCallBack callBack) {
        FirebaseFirestore database = FirebaseFirestore.getInstance(); // Получаем экземпляр Firebase Firestore
        CollectionReference usersRef = database.collection(Constants.KEY_COLLECTION_USERS); // ссылка на коллекцию пользователей
        usersRef.whereEqualTo("name", name).get()
                .addOnCompleteListener(task -> { // запрос к Firebase Firestore, чтобы проверить, существует ли уже такое имя пользователя
                    if (task.isSuccessful()) {
                        boolean isExist = false;
                        for (QueryDocumentSnapshot document : task.getResult()) { // Перебираем результаты запроса
                            if (document.exists()){
                                isExist = true; // если имя пользователя уже существует, устанавливаем флаг isExist
                                binding.textError.setText("The user with this name has already been registered"); // выводим сообщение об ошибке
                                break;
                            }
                        }
                        callBack.onCompleted(isExist); // вызываем callback с результатом проверки
                    } else {
                        showToast("Error"); // В случае ошибки выводим сообщение
                    }
                });
    }

    // Функция вызывающая проверку имени пользователя
    private void isValidName() {
        checkIfNameExists(binding.inputName.getText().toString().trim(), isExist -> {
            isValidName = !isExist; // Обновление значения isValidName
        });
    }

    // Валидация формы регистрации
    @SuppressLint("SetTextI18n")
    private Boolean isValidSignUpDetails() {
        // Последовательная проверка каждого условия
        // Если условие не выполняется, выводим соответствующее сообщение и возвращаем false
        if(encodedImage == null) {
            showToast("Select profile image");
            return false;
        } else if(binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if(binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password & confirm password must be same");
            return false;
        } else if(!isValidEmail) {
            return false;
        } else if(!isValidName) {
            return false;
        } else { // Если все условия выполняются, выводим сообщение об успешной валидации и возвращаем true
            binding.textSuccessful.setText("That's right, welcome to our messenger!");
            return true;
        }
    }

    // Функция управления отображением элементов интерфейса во время загрузки
    private void loading(Boolean isLoading) {
        if(isLoading) { // Если процесс загрузки активен, показываем индикатор загрузки и скрываем кнопку регистрации
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else { // Если процесс загрузки закончен, скрываем индикатор загрузки и показываем кнопку регистрации
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}


