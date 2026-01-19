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

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Inicializar SessionManager y ViewModel
        SessionManager sessionManager = new SessionManager(this);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Configurar evento de clic
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(email, password);
            } else {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        // Observador para el éxito del login
        loginViewModel.getLoginResponse().observe(this, response -> {
            progressBar.setVisibility(View.GONE);

            // Guardar el token JWT y el nombre del usuario de forma segura
            sessionManager.saveAuthToken(response.getToken(), response.getUsuario());

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
}