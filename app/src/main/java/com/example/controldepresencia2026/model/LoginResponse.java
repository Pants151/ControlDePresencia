package com.example.controldepresencia2026.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String token;
    private String usuario;
    private String msg;

    @SerializedName(value = "rol", alternate = { "role", "roles", "tipo_usuario", "user_role" })
    private String rol;

    // Getters
    public String getToken() {
        return token;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getMsg() {
        return msg;
    }

    public String getRol() {
        return rol;
    }
}