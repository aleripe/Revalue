package it.returntrue.revalue.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Provides session preferences management
 */
public class SessionPreferences {
    private static final String PREFERENCES_SESSION = "it.returntrue.revalue.PREFERENCES_FILE_SESSION";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ALIAS = "alias";
    private static final String KEY_AVATAR = "avatar";

    private SharedPreferences mPreferences;

    public SessionPreferences(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_SESSION, Context.MODE_PRIVATE);
    }

    public void setUserId(int userId) {
        mPreferences.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return mPreferences.getInt(KEY_USER_ID, 0);
    }

    public void setUsername(String username) {
        mPreferences.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return mPreferences.getString(KEY_USERNAME, null);
    }

    public void setToken(String token) {
        mPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return mPreferences.getString(KEY_TOKEN, null);
    }

    public void setAlias(String alias) {
        mPreferences.edit().putString(KEY_ALIAS, alias).apply();
    }

    public String getAlias() {
        return mPreferences.getString(KEY_ALIAS, null);
    }

    public void setAvatar(String avatar) {
        mPreferences.edit().putString(KEY_AVATAR, avatar).apply();
    }

    public String getAvatar() {
        return mPreferences.getString(KEY_AVATAR, null);
    }

    public boolean getIsLoggedIn() {
        return !TextUtils.isEmpty(getToken());
    }

    public void login(int userId, String username, String token, String alias, String avatar) {
        setUserId(userId);
        setUsername(username);
        setToken(token);
        setAlias(alias);
        setAvatar(avatar);
    }

    public void logout() {
        mPreferences.edit().remove(KEY_USERNAME).remove(KEY_TOKEN).apply();
    }
}