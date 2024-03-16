package com.qwy_games.fllafmessenger.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qwy_games.fllafmessenger.databinding.ItemContainerUserBinding;
import com.qwy_games.fllafmessenger.listeners.UserListener;
import com.qwy_games.fllafmessenger.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users; // Список пользователей
    private final UserListener userListener; // Слушатель событий на выбор пользователя

    // Конструктор адаптера
    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    // Создание нового ViewHolder
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    // Привязка данных к ViewHolder
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position)); // Привязка данных пользователя к вьюхолдеру
    }

    // Возврат количества элементов в списке
    @Override
    public int getItemCount() {
        return users.size();
    }

    // ViewHolder для отдельного элемента в списке
    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;

        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        // Метод для связывания данных пользователя с элементами пользовательского интерфейса
        void setUserData(User user) {
            binding.textName.setText(user.name); // Установка имени пользователя
            binding.textEmail.setText(user.email); // Установка адреса электронной почты пользователя
            binding.imageProfile.setImageBitmap(getUserImage(user.image)); // Установка изображения профиля пользователя
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user)); // Установка слушателя нажатия на элемент списка
        }
    }

    // Метод для конвертации строки в кодировке Base64 в изображение
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}