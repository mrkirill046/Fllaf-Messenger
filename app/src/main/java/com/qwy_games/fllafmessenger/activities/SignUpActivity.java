package com.qwy_games.fllafmessenger.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.qwy_games.fllafmessenger.R;
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
    private String encodedImage;
    private  boolean isValidEmail;
    private  boolean isValidName;

    // Отрисовка activity_sign_up.xml и инициализация всего необходимого
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    // Функция для кнопок, чтобы они работали
    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            binding.textError.setText("");

            isValidEmail();
            isValidName();

            if(isValidSignUpDetails()) {
                binding.textSuccessful.setText("That's right, press the button again!");
                binding.textError.setText("");
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Функция для регистрирования новой учетной записи
    private void signUp() {
        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        // Заполнение HashMap user данными, введными пользователем в форме
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user) // Добавление пользователя в базу данных
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    String userName = binding.inputName.getText().toString();

                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, userName);
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    showToast(exception.getMessage());
                });
    }

    // Зашифровка аватарки пользователя
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = 150 /* bitmap.getHeight() * previewWidth / bitmap.getWidth() */;
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
          if(result.getResultCode() == RESULT_OK) {
              if(result.getData() != null) {
                  Uri imageUri = result.getData().getData();
                  try {
                      InputStream inputStream = getContentResolver().openInputStream(imageUri);
                      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                      binding.imageProfile.setImageBitmap(bitmap);
                      binding.textAddImage.setVisibility(View.GONE);
                      encodedImage = encodeImage(bitmap);
                  } catch (FileNotFoundException e) {
                      e.printStackTrace();
                  }
              }
          }
      }
    );

    // Проверка одиннаковых email
    interface EmailExistCallBack {
        void onCompleted(boolean isExist);
    }

    private void checkIfEmailExists(String email, EmailExistCallBack callBack) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersRef = database.collection(Constants.KEY_COLLECTION_USERS);
        usersRef.whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isExist = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()){
                                isExist = true;
                                binding.textError.setText("The user with this email has already been registered");
                                break;
                            }
                        }
                        callBack.onCompleted(isExist);
                    } else {
                        showToast("Error");
                    }
                });
    }

    private void isValidEmail() {
        checkIfEmailExists(binding.inputEmail.getText().toString().trim(), isExist -> {
            isValidEmail = !isExist;
        });
    }

    // Проверка одиннаковых name
    interface NameExistCallBack {
        void onCompleted(boolean isExist);
    }

    private void checkIfNameExists(String email, NameExistCallBack callBack) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersRef = database.collection(Constants.KEY_COLLECTION_USERS);
        usersRef.whereEqualTo("name", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isExist = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()){
                                isExist = true;
                                binding.textError.setText("The user with this name has already been registered");
                                break;
                            }
                        }
                        callBack.onCompleted(isExist);
                    } else {
                        showToast("Error");
                    }
                });
    }

    private void isValidName() {
        checkIfNameExists(binding.inputName.getText().toString().trim(), isExist -> {
            isValidName = !isExist;
        });
    }

    // Валидация формы
    private Boolean isValidSignUpDetails() {
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
        } else {
            binding.textSuccessful.setText("That's right, press the button again!");
            return true;
        }
    }

    // Отображение загрузки
    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}