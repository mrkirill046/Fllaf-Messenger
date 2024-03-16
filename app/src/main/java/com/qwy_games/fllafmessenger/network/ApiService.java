package com.qwy_games.fllafmessenger.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiService {

    // Метод для отправки сообщения с помощью HTTP POST запроса
    @POST("send") // endpoint (путь) для отправки запроса
    Call<String> sendMessage(
            @HeaderMap HashMap<String, String> headers, // Заголовки запроса
            @Body String messageBody // Тело запроса
            );
}
