package com.example.controldepresencia2026.utils;

import android.util.Base64;
import android.util.Log;
import org.json.JSONObject;

public class JwtUtils {

    public static String getUserRole(String token) {
        try {
            // El token tiene 3 partes: Header.Payload.Signature
            String[] split = token.split("\\.");
            if (split.length < 2)
                return null;

            // Decodificamos el Payload
            String payload = getJson(split[1]);
            JSONObject jsonObject = new JSONObject(payload);

            // Buscamos el rol
            if (jsonObject.has("rol")) {
                return jsonObject.getString("rol");
            }

            return null;

        } catch (Exception e) {
            Log.e("JWT_DECODE", "Error decoding JWT", e);
            return null;
        }
    }

    private static String getJson(String strEncoded) throws Exception {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
