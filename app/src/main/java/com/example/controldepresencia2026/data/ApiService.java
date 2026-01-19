package com.example.controldepresencia2026.data;

import com.example.controldepresencia2026.model.LoginRequest;
import com.example.controldepresencia2026.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}