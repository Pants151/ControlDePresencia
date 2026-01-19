package com.example.controldepresencia2026.model;

public class LoginResponse {
    private String token;
    private String usuario;
    private String msg; // Para capturar mensajes de error como "Credenciales incorrectas"

    // Getters
    public String getToken() { return token; }
    public String getUsuario() { return usuario; }
    public String getMsg() { return msg; }
}