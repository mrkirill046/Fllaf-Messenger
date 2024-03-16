package com.qwy_games.fllafmessenger.adapters;

import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qwy_games.fllafmessenger.databinding.ItemContainerRecentConversionBinding;
import com.qwy_games.fllafmessenger.listeners.ConversionListener;
import com.qwy_games.fllafmessenger.models.ChatMessage;
import com.qwy_games.fllafmessenger.models.User;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages; // Список объектов сообщений чата
    private final ConversionListener conversionListener; // Слушатель нажатий на элемент списка

    // Конструктор адаптера
    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    // Создание нового ViewHolder
    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(ItemContainerRecentConversionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        ));
    }

    // Привязка данных к ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    // Возврат количества элементов в списке
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder для отдельного элемента в списке
    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding =itemContainerRecentConversionBinding;
        }

        // Функция для привязки данных к элементам пользовательского интерфейса
        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage)); // Конвертация изображения из Base64 в Bitmap и установка его в imageView
            binding.textName.setText(chatMessage.conversionName); // Установка имени пользователя в TextView
            binding.textRecentMessage.setText(chatMessage.message); // Установка последнего сообщения диалога в TextView
            binding.getRoot().setOnClickListener(v -> { // Установка слушателя нажатий на элемент списка
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListener.onConversionListener(user);
            });
        }
    }

    // Функция для конвертации Base64 строки в растровое изображение
    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}