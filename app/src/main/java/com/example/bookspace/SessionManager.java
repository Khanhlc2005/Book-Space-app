package com.example.bookspace;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREFS_NAME = "bookspace_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_EMAIL = "login_email";

    private SessionManager() {
    }

    public static boolean isLoggedIn(Context context) {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void login(Context context, String email) {
        getPreferences(context)
                .edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_LOGIN_EMAIL, email)
                .apply();
    }

    public static void logout(Context context) {
        getPreferences(context).edit().clear().apply();
    }

    public static String getLoginEmail(Context context) {
        return getPreferences(context).getString(KEY_LOGIN_EMAIL, "");
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
