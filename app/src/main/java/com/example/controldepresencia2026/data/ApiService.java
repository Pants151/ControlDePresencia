package com.example.controldepresencia2026.data;

import com.example.controldepresencia2026.model.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/presencia/entrada")
    Call<BasicResponse> registrarEntrada(@Header("Authorization") String token, @Body LocationData location);

    @POST("api/presencia/salida")
    Call<BasicResponse> registrarSalida(@Header("Authorization") String token, @Body LocationData location);

    @GET("api/presencia/estado")
    Call<EstadoResponse> obtenerEstado(@Header("Authorization") String token);

    // Para incidencias, usaremos un Map o una clase simple para el JSON
    @POST("api/incidencias")
    Call<BasicResponse> registrarIncidencia(@Header("Authorization") String token, @Body java.util.Map<String, String> body);
}