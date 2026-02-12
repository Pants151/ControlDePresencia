package com.example.controldepresencia2026.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.model.*;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends ViewModel {
    private MutableLiveData<EstadoResponse> estado = new MutableLiveData<>();
    private MutableLiveData<String> mensajeExito = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<ResumenResponse> resumenMensual = new MutableLiveData<>();

    public LiveData<EstadoResponse> getEstado() { return estado; }
    public LiveData<String> getMensajeExito() { return mensajeExito; }
    public LiveData<String> getError() { return error; }
    public LiveData<ResumenResponse> getResumenMensual() { return resumenMensual; }

    // Obtener si el trabajador ya ha fichado hoy
    public void consultarEstado(String token) {
        RetrofitClient.getApiService().obtenerEstado("Bearer " + token).enqueue(new Callback<EstadoResponse>() {
            @Override
            public void onResponse(Call<EstadoResponse> call, Response<EstadoResponse> response) {
                if (response.isSuccessful()) estado.setValue(response.body());
            }
            @Override
            public void onFailure(Call<EstadoResponse> call, Throwable t) {
                error.setValue("Fallo al consultar estado: " + t.getMessage());
            }
        });
    }

    // Obtener resumen mensual
    public void consultarResumen(String token) {
        RetrofitClient.getApiService().obtenerResumenMensual("Bearer " + token, null)
                .enqueue(new Callback<ResumenResponse>() {
                    @Override
                    public void onResponse(Call<ResumenResponse> call, Response<ResumenResponse> response) {
                        if (response.isSuccessful()) resumenMensual.setValue(response.body());
                    }
                    @Override
                    public void onFailure(Call<ResumenResponse> call, Throwable t) {}
                });
    }

    // Fichar Entrada con GPS
    public void ficharEntrada(String token, double lat, double lng) {
        LocationData location = new LocationData(lat, lng);
        RetrofitClient.getApiService().registrarEntrada("Bearer " + token, location).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    mensajeExito.setValue(response.body().getMsg());
                    consultarEstado(token);
                } else {
                    // LEER EL ERROR REAL DEL SERVIDOR
                    try {
                        String errorJson = response.errorBody().string();
                        // Esto mostrará si es "Fuera de radio" o "Entrada activa"
                        error.setValue("Error Servidor: " + errorJson);
                    } catch (Exception e) {
                        error.setValue("Error desconocido");
                    }
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                error.setValue("Fallo de red: " + t.getMessage());
            }
        });
    }

    // Fichar Salida
    public void ficharSalida(String token, double lat, double lng) {
        // Creamos el objeto con las coordenadas actuales
        LocationData location = new LocationData(lat, lng);

        RetrofitClient.getApiService().registrarSalida("Bearer " + token, location).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mensajeExito.setValue(response.body().getMsg());
                    consultarEstado(token);
                    consultarResumen(token);
                } else {
                    error.setValue("Error al salir: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                error.setValue("Error de red: " + t.getMessage());
            }
        });
    }

    // Enviar Incidencia
    public void enviarIncidencia(String token, String descripcion) {
        Map<String, String> body = new HashMap<>();
        body.put("descripcion", descripcion);
        RetrofitClient.getApiService().registrarIncidencia("Bearer " + token, body).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    mensajeExito.setValue("Incidencia enviada correctamente");
                } else {
                    error.setValue("Error al enviar: Código " + response.code());
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                error.setValue("Fallo de red: " + t.getMessage());
            }
        });
    }
}