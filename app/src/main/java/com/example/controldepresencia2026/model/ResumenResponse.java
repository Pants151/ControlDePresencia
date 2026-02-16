package com.example.controldepresencia2026.model;

import com.google.gson.annotations.SerializedName;

public class ResumenResponse {

    @SerializedName("mes")
    private String mes;

    @SerializedName("horas_reales")
    private double horasReales;

    @SerializedName("horas_teoricas")
    private double horasTeoricas;

    @SerializedName("horas_extra")
    private double horasExtra;

    // Constructor vacío requerido por GSON
    public ResumenResponse() {
    }

    // Getters
    public String getMes() {
        return mes;
    }

    public double getHorasReales() {
        return horasReales;
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

    public void setHorasReales(double horasReales) {
        this.horasReales = horasReales;
    }

    public void setHorasTeoricas(double horasTeoricas) {
        this.horasTeoricas = horasTeoricas;
    }

    public void setHorasExtra(double horasExtra) {
        this.horasExtra = horasExtra;
    }
}