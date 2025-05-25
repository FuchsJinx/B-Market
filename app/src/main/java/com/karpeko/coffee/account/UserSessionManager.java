package com.karpeko.coffee.account;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private static final String PREFS_NAME = "accountPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String PREFS_FAVORITES = "inFavorite";

    private SharedPreferences prefs;

    public UserSessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public boolean isFavorite(String itemId) {
        return prefs.getBoolean("favorite_" + itemId, false);
    }

    public void setFavorite(String itemId, boolean isFavorite) {
        prefs.edit().putBoolean("favorite_" + itemId, isFavorite).apply();
    }

    public void setUserLoggedIn(String userId) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}

