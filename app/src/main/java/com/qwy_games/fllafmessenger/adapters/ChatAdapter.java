package com.qwy_games.fllafmessenger.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qwy_games.fllafmessenger.databinding.ItemContainerSendMessageBinding;
import com.qwy_games.fllafmessenger.databinding.ItemContainerReceivedMessageBinding;
import com.qwy_games.fllafmessenger.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages; // Список объектов сообщений чата
    private Bitmap receiverProfileImage; // Изображение профиля получателя сообщений
    private final String senderId; // идентификатор отправителя сообщения

    // Константы представляющие разные типы представлений
    private static final int VIEW_TYPE_SEND = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    // Метод для установки изображения профиля получателя
    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    // Конструктор адаптера
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    // Здесь создаются представления для различных типов элементов
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SEND) {
            return new SendMessageViewHolder(
                    ItemContainerSendMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    // Здесь данные привязываются к каждому представлению элемента
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SEND) {
            ((SendMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    // Здесь возвращается количество элементов в списке
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // Возвращает тип представления элемента
    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SEND;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    // Класс ViewHolder для отправленных сообщений
    static class SendMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSendMessageBinding binding;

        SendMessageViewHolder(ItemContainerSendMessageBinding ItemContainerSendMessageBinding) {
            super(ItemContainerSendMessageBinding.getRoot());
            binding = ItemContainerSendMessageBinding;
        }

        // Здесь привязываем данные к элементам представления
        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dataTime);
        }
    }

    // Класс ViewHolder для полученных сообщений
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding ItemContainerReceivedMessageBinding) {
            super(ItemContainerReceivedMessageBinding.getRoot());
            binding = ItemContainerReceivedMessageBinding;
        }

        // Здесь привязываем данные к элементам представления
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dataTime);
            if(receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}