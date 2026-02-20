package com.example.controldepresencia2026.model;

import com.google.gson.annotations.SerializedName;

public class TrabajadorSimple {
    @SerializedName(value = "id", alternate = { "id_usuario", "user_id", "id_trabajador" })
    private int id;

    @SerializedName(value = "nombre", alternate = { "name", "username", "usuario" })
    private String nombre;

    @SerializedName(value = "email", alternate = { "correo", "mail" })
    private String email;

    public TrabajadorSimple(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public TrabajadorSimple(int id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return nombre; // Para que el Spinner muestre el nombre directamente
    }
}
