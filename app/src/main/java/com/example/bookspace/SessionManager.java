package com.example.bookspace;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREFS_NAME = "bookspace_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_USERNAME = "login_username";

    private SessionManager() {
    }

    public static boolean isLoggedIn(Context context) {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void login(Context context, String username) {
        getPreferences(context)
                .edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_LOGIN_USERNAME, username)
                .apply();
    }

    public static void logout(Context context) {
        getPreferences(context).edit().clear().apply();
    }

    public static String getLoginUsername(Context context) {
        return getPreferences(context).getString(KEY_LOGIN_USERNAME, "");
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
