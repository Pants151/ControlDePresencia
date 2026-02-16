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
import org.json.JSONObject;

public class MainViewModel extends ViewModel {
    private MutableLiveData<EstadoResponse> estado = new MutableLiveData<>();
    private MutableLiveData<String> mensajeExito = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<ResumenResponse> resumenMensual = new MutableLiveData<>();
    public LiveData<EstadoResponse> getEstado() {
        return estado;
    }
    public LiveData<String> getMensajeExito() {
        return mensajeExito;
    }
    public LiveData<String> getError() {
        return error;
    }
    public LiveData<ResumenResponse> getResumenMensual() {
        return resumenMensual;
    }

    // Obtener si el trabajador ya ha fichado hoy
    public void consultarEstado(String token) {
        RetrofitClient.getApiService().obtenerEstado("Bearer " + token).enqueue(new Callback<EstadoResponse>() {
            @Override
            public void onResponse(Call<EstadoResponse> call, Response<EstadoResponse> response) {
                if (response.isSuccessful())
                    estado.setValue(response.body());
            }

            @Override
            public void onFailure(Call<EstadoResponse> call, Throwable t) {
                error.setValue(com.example.controldepresencia2026.utils.ErrorUtils.getFriendlyMessage(t));
            }
        });
    }

    // Obtener resumen mensual
    public void consultarResumen(String token) {
        RetrofitClient.getApiService().obtenerResumenMensual("Bearer " + token, null)
                .enqueue(new Callback<ResumenResponse>() {
                    @Override
                    public void onResponse(Call<ResumenResponse> call, Response<ResumenResponse> response) {
                        if (response.isSuccessful())
                            resumenMensual.setValue(response.body());
                    }

                    @Override
                    public void onFailure(Call<ResumenResponse> call, Throwable t) {
                        error.setValue(com.example.controldepresencia2026.utils.ErrorUtils.getFriendlyMessage(t));
                    }
                });
    }

    // Fichar Entrada con GPS
    public void ficharEntrada(String token, double lat, double lng) {
        LocationData location = new LocationData(lat, lng);
        RetrofitClient.getApiService().registrarEntrada("Bearer " + token, location)
                .enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        if (response.isSuccessful()) {
                            mensajeExito.setValue(response.body().getMsg());
                            consultarEstado(token);
                        } else {
                            manejarErroresHttp(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        error.setValue(com.example.controldepresencia2026.utils.ErrorUtils.getFriendlyMessage(t));
                    }
                });
    }

    // Fichar Salida
    public void ficharSalida(String token, double lat, double lng) {
        // Creamos el objeto con las coordenadas actuales
        LocationData location = new LocationData(lat, lng);

        RetrofitClient.getApiService().registrarSalida("Bearer " + token, location)
                .enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            mensajeExito.setValue(response.body().getMsg());
                            consultarEstado(token);
                            consultarResumen(token);
                        } else {
                            manejarErroresHttp(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        error.setValue(com.example.controldepresencia2026.utils.ErrorUtils.getFriendlyMessage(t));
                    }
                });
    }

    private void manejarErroresHttp(Response<?> response) {
        if (response.code() == 404) {
            // El empleado ha sido borrado de la base de datos
            error.setValue("Error crítico: Su cuenta ha sido dada de baja.");
        } else if (response.code() == 401) {
            // Token expirado o inválido
            error.setValue("Sesión expirada. Por favor, entre de nuevo.");
        } else if (response.code() == 500) {
            // Error interno del servidor (posible bug o estado inconsistente)
            error.setValue("Error interno del servidor. Contacte con soporte.");
        } else {
            error.setValue(parsearError(response));
        }
    }

    // Enviar Incidencia
    public void enviarIncidencia(String token, String descripcion) {
        Map<String, String> body = new HashMap<>();
        body.put("descripcion", descripcion);
        RetrofitClient.getApiService().registrarIncidencia("Bearer " + token, body)
                .enqueue(new Callback<BasicResponse>() {
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
                        error.setValue(com.example.controldepresencia2026.utils.ErrorUtils.getFriendlyMessage(t));
                    }
                });

    }

    // Parsear errores HTTP
    private String parsearError(Response<?> response) {
        try {
            if (response.errorBody() == null)
                return "Error desconocido: " + response.code();
            String errorBody = response.errorBody().string();
            JSONObject json = new JSONObject(errorBody);
            // flask_smorest usa "message", flask_jwt usa "msg"
            if (json.has("message"))
                return json.getString("message");
            if (json.has("msg"))
                return json.getString("msg");
            return "Error: " + response.code();
        } catch (Exception e) {
            return "Error de conexión con el servidor (" + response.code() + ")";
        }
    }
}