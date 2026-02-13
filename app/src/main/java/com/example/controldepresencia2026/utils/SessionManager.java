package com.example.controldepresencia2026.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {
    private static final String PREF_NAME = "secure_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_NAME = "user_name"; //
    private static final String USER_ROL = "user_rol";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            editor = sharedPreferences.edit();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback para emuladores (usamos otro fichero para evitar conflictos con el
            // encriptado)
            sharedPreferences = context.getSharedPreferences(PREF_NAME + "_fallback", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

    public void saveAuthToken(String token, String userName) {
        if (editor == null)
            return;
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_NAME, userName); // Guardamos el nombre
        editor.apply();
    }

    public String fetchAuthToken() {
        if (sharedPreferences == null)
            return null;
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String fetchUserName() {
        if (sharedPreferences == null)
            return "Usuario";
        return sharedPreferences.getString(KEY_USER_NAME, "Usuario");
    }

    public void saveUserRol(String rol) {
        if (editor == null)
            return;
        editor.putString(USER_ROL, rol);
        editor.apply();
    }

    public String fetchUserRol() {
        if (sharedPreferences == null)
            return null;
        return sharedPreferences.getString(USER_ROL, null);
    }

    public void logout() {
        if (editor == null)
            return;
        editor.clear();
        editor.apply();
    }

    public void setRememberMe(boolean remember) {
        if (editor == null)
            return;
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        editor.apply();
    }

    public boolean isRememberMe() {
        if (sharedPreferences == null)
            return false;
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }
}