package com.example.controldepresencia2026.data;

import com.example.controldepresencia2026.model.BasicResponse;
import com.example.controldepresencia2026.model.EstadoResponse;
import com.example.controldepresencia2026.model.GeoResponse;
import com.example.controldepresencia2026.model.LocationData;
import com.example.controldepresencia2026.model.LoginRequest;
import com.example.controldepresencia2026.model.LoginResponse;
import com.example.controldepresencia2026.model.RegistroAdmin;
import com.example.controldepresencia2026.model.ResumenResponse;
import com.example.controldepresencia2026.model.TrabajadorSimple;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

public interface ApiService {
        @POST("api/auth/login")
        Call<LoginResponse> login(@Body LoginRequest loginRequest);

        @POST("api/presencia/entrada")
        Call<BasicResponse> registrarEntrada(@Header("Authorization") String token, @Body LocationData location);

        @POST("api/presencia/salida")
        Call<BasicResponse> registrarSalida(@Header("Authorization") String token, @Body LocationData location);

        @GET("api/presencia/estado")
        Call<EstadoResponse> obtenerEstado(@Header("Authorization") String token);

        // Para incidencias
        @POST("api/incidencias")
        Call<BasicResponse> registrarIncidencia(@Header("Authorization") String token,
                        @Body java.util.Map<String, String> body);

        // PARA EL MAPA
        @GET("api/empresa/config")
        Call<Map<String, Object>> obtenerConfigEmpresa(@Header("Authorization") String token);

        @POST("api/empresa/config")
        Call<BasicResponse> actualizarConfigEmpresa(
                        @Header("Authorization") String token,
                        @Body Map<String, Object> body);

        @GET("api/presencia/resumen-mensual")
        Call<ResumenResponse> obtenerResumenMensual(
                        @Header("Authorization") String token,
                        @Query("mes") String mes);

        @GET("api/admin/registros")
        Call<List<RegistroAdmin>> obtenerRegistrosAdmin(
                        @Header("Authorization") String token,
                        @Query("id_trabajador") String idTrabajador);

        @GET("api/admin/registros-recientes")
        Call<List<RegistroAdmin>> getRegistrosRecientes(@Header("Authorization") String token);

        @GET("api/empresa/configuracion-geo")
        Call<GeoResponse> getGeoConfig(@Header("Authorization") String token);

        @GET("api/admin/trabajadores")
        Call<List<TrabajadorSimple>> obtenerTrabajadores(@Header("Authorization") String token);

        @POST("api/auth/forgot-password")
        Call<BasicResponse> recuperarContrasena(@Body Map<String, String> emailBody);

        @POST("api/auth/change-password")
        Call<BasicResponse> cambiarContrasena(@Header("Authorization") String token, @Body Map<String, String> body);

        @POST("api/usuario/actualizar-fcm")
        Call<BasicResponse> actualizarTokenFCM(@Header("Authorization") String token, @Body Map<String, String> body);

        @POST("api/usuario/logout-fcm")
        Call<BasicResponse> logoutFCM(@Header("Authorization") String token);

        // OBTENER FICHAJES DEL USUARIO ACTUAL
        @GET("api/mis-registros")
        Call<List<RegistroAdmin>> getMisRegistros(@Header("Authorization") String token);

        // OBTENER TODOS LOS TRABAJADORES
        @GET("api/admin/trabajadores")
        Call<List<TrabajadorSimple>> getTrabajadores(@Header("Authorization") String token);
}