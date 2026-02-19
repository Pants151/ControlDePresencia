package com.example.controldepresencia2026.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.controldepresencia2026.MainActivity;
import com.example.controldepresencia2026.R;
import com.example.controldepresencia2026.utils.SessionManager;
import com.example.controldepresencia2026.viewmodel.LoginViewModel;
import android.text.InputType;
import android.widget.TextView;
import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.model.BasicResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.app.AlertDialog;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private EditText etEmail, etPassword;
    private android.widget.CheckBox cbRememberMe;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Inicializar SessionManager y ViewModel
        SessionManager sessionManager = new SessionManager(this);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Auto-Login
        if (sessionManager.fetchAuthToken() != null && sessionManager.isRememberMe()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Configurar evento de clic
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                // progressBar.setVisibility(View.VISIBLE); // Removed as per instruction
                // snippet
                loginViewModel.login(email, password);
            } else {
                Toast.makeText(this, "Por favor, ingrese credenciales", Toast.LENGTH_SHORT).show();
            }
        });

        // Recuperar contraseña
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            final EditText inputEmail = new EditText(this);
            inputEmail.setHint("ejemplo@correo.com");
            inputEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            new AlertDialog.Builder(this)
                    .setTitle("Recuperar Contraseña")
                    .setMessage("Introduce tu email para recibir una clave temporal:")
                    .setView(inputEmail)
                    .setPositiveButton("Enviar", (dialog, which) -> {
                        String email = inputEmail.getText().toString().trim();
                        if (!email.isEmpty()) {
                            ejecutarRecuperacion(email);
                        } else {
                            Toast.makeText(this, "El email es obligatorio", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Observador para el éxito del login
        loginViewModel.getLoginResponse().observe(this, response -> {
            progressBar.setVisibility(View.GONE);

            // Guardar el token JWT y el nombre del usuario de forma segura
            String token = response.getToken();
            String rol = response.getRol();

            // Si el rol no viene en el JSON directo, lo sacamos del Token
            if (rol == null) {
                try {
                    rol = com.example.controldepresencia2026.utils.JwtUtils.getUserRole(token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            sessionManager.saveAuthToken(token, response.getUsuario());
            sessionManager.saveUserRol(rol);
            sessionManager.setRememberMe(cbRememberMe.isChecked());

            Toast.makeText(this, "Bienvenido " + response.getUsuario(), Toast.LENGTH_SHORT).show();

            // Ir a la pantalla principal
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Observador para errores
        loginViewModel.getErrorMessage().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    // Recuperar contraseña
    private void ejecutarRecuperacion(String email) {
        Map<String, String> data = new HashMap<>();
        data.put("email", email);

        RetrofitClient.getApiService().recuperarContrasena(data).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Si el email existe, recibirás un correo con el enlace.", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string()
                                : "Error desconocido";
                        Toast.makeText(LoginActivity.this, "Error " + response.code() + ": " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Error " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}