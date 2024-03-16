package com.qwy_games.fllafmessenger.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences; // Объект SharedPreferences для хранения данных

    // Конструктор класса
    public PreferenceManager(Context context) {
        // Создаем объект SharedPreferences
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    // Метод для сохранения boolean значения в SharedPreferences
    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Метод для получения сохраненного boolean значения из SharedPreferences
    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    // Метод для сохранения строки в SharedPreferences
    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // Метод для получения сохраненной строки из SharedPreferences
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    // Метод для очистки всех данных из SharedPreferences
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}