package com.qwy_games.fllafmessenger.network;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    // Статический объект Retrofit, используемый для выполнения HTTP-запросов
    private static Retrofit retrofit = null;

    // Метод для получения экземпляра клиента Retrofit
    public static Retrofit getClient() {
        // Если экземпляр еще не был создан, создаем новый
        if(retrofit == null) {
            retrofit = new Retrofit.Builder() // Создаем новый экземпляр Retrofit с помощью конструктора
                    .baseUrl("https://fcm.googleapis.com/fcm/") // Устанавливаем базовый URL для выполнения HTTP-запросов
                    .addConverterFactory(ScalarsConverterFactory.create()) // Устанавливаем конвертер, который будет использоваться для преобразования HTTP-ответов в данных
                    .build(); // Собираем и возвращаем созданный экземпляр Retrofit
        }
        // Возвращаем существующий или только что созданный экземпляр Retrofit
        return retrofit;
    }
}
