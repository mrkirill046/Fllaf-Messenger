package com.qwy_games.fllafmessenger.listeners;

import com.qwy_games.fllafmessenger.models.User;

public interface UserListener {

    // Метод, который вызывается, когда кликнули по элементу в списке пользователей
    void onUserClicked(User user);
}
