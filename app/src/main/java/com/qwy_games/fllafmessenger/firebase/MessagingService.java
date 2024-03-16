package com.qwy_games.fllafmessenger.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.qwy_games.fllafmessenger.R;
import com.qwy_games.fllafmessenger.activities.ChatActivity;
import com.qwy_games.fllafmessenger.models.User;
import com.qwy_games.fllafmessenger.utilities.Constants;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    // Метод вызывается при обновлении токена. Реализация FirebaseMessagingService обрабатывает это за вас,
    // но вы можете переопределить этот метод, если хотите выполнять дополнительные действия после обновления токена.
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    // Этот метод будет вызываться при получении нового сообщения из Firebase Cloud Messaging
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        User user = new User(); // Создаем новый объект пользователя
        user.id = remoteMessage.getData().get(Constants.KEY_USER_ID); // Получаем идентификатор пользователя из полученного сообщения
        user.name = remoteMessage.getData().get(Constants.KEY_NAME); // Получаем имя пользователя из полученного сообщения
        user.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN); // Получаем Firebase-токен из полученного сообщения

        int notificationId = new Random().nextInt(); // Создаем случайный идентификатор для уведомления
        String channelId = "chat_message"; // Канал уведомления

        Intent intent = new Intent(this, ChatActivity.class); // Интент на открытие активности чата
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Устанавливаем флаги для интента
        intent.putExtra(Constants.KEY_USER, user); // Помещаем объект пользователя в интент как extra
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0); // Создаем ожидающий интент, который будет запускать активность чата

        // Создаем построитель уведомлений
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notifications); // Устанавливаем иконку уведомления
        builder.setContentTitle(user.name); // Устанавливаем заголовок уведомления
        builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE)); // Устанавливаем текст уведомления
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get(Constants.KEY_MESSAGE)
        )); // Устанавливаем стиль уведомления, чтобы оно могло отображать больше текста
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT); // Устанавливаем приоритет уведомления
        builder.setContentIntent(pendingIntent); // Устанавливаем ожидающий интент, который будет запускаться при нажатии на уведомление
        builder.setAutoCancel(true); // Устанавливаем автоотмену уведомления при его нажатии

        // Создаем канал уведомлений для Android O и выше
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Отображаем уведомление
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());
    }
}