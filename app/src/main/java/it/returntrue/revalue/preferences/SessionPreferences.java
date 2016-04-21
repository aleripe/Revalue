package it.returntrue.revalue.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Provides session preferences management
 */
public class SessionPreferences {
    private static final String PREFERENCES_SESSION = "session";
    private static final String KEY_TOKEN = "token";

    private SharedPreferences mPreferences;

    public SessionPreferences(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
    }

    public void setToken(String token) {
        mPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return mPreferences.getString(KEY_TOKEN, "");
    }

    public boolean getIsLoggedIn() {
        return !TextUtils.isEmpty(getToken());
    }
}