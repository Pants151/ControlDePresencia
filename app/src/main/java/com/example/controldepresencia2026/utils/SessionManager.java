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
    private static final String KEY_USER_NAME = "user_name";

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
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            editor = sharedPreferences.edit();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    // Guardar token y nombre al loguearse
    public void saveAuthToken(String token, String userName) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    // Obtener el token para las peticiones API
    public String fetchAuthToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Borrar sesión (Logout)
    public void logout() {
        editor.clear();
        editor.apply();
    }
}