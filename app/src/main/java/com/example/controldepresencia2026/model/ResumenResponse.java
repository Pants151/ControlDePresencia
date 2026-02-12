package com.example.controldepresencia2026.model;

import com.google.gson.annotations.SerializedName;

public class ResumenResponse {

    @SerializedName("mes")
    private String mes;

    @SerializedName("horas_trabajadas")
    private double horasTrabajadas;

    @SerializedName("horas_teoricas")
    private double horasTeoricas;

    @SerializedName("horas_extra")
    private double horasExtra;

    // Constructor vacío requerido por GSON
    public ResumenResponse() {}

    // Getters
    public String getMes() {
        return mes;
    }

    public double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public double getHorasTeoricas() {
        return horasTeoricas;
    }

    public double getHorasExtra() {
        return horasExtra;
    }

    // Setters
    public void setMes(String mes) {
        this.mes = mes;
    }

    public void setHorasTrabajadas(double horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    public void setHorasTeoricas(double horasTeoricas) {
        this.horasTeoricas = horasTeoricas;
    }

    public void setHorasExtra(double horasExtra) {
        this.horasExtra = horasExtra;
    }
}