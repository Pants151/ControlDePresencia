package com.example.controldepresencia2026.view;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controldepresencia2026.R;
import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.model.BasicResponse;
import com.example.controldepresencia2026.utils.SessionManager;

import org.osmdroid.config.Configuration;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        sessionManager = new SessionManager(this);
        String token = sessionManager.fetchAuthToken();
        String rol = sessionManager.fetchUserRol();

        if (token == null) {
            finish();
            return;
        }

        // Configuración OSMDroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        // Initializing views
        Button btnAdmin = findViewById(R.id.btnAdminConfig);
        Button btnChangePass = findViewById(R.id.btnChangePasswordConfig);
        Button btnLogout = findViewById(R.id.btnLogoutConfig);
        Button btnMisFichajes = findViewById(R.id.btnMisFichajes);

        // Visibilidad Botón Admin
        if ("Administrador".equals(rol) || "Superadministrador".equals(rol)) {
            btnAdmin.setVisibility(View.VISIBLE);
            btnAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));
        }

        // Si es administrador, ocultamos "Mis Fichajes" porque ya tiene su superpanel
        if ("Administrador".equals(rol) || "Superadministrador".equals(rol)) {
            btnMisFichajes.setVisibility(View.GONE);
        }

        btnMisFichajes.setOnClickListener(v -> {
            startActivity(new Intent(ConfigActivity.this, MisRegistrosActivity.class));
        });

        // Botón Cambiar Contraseña
        btnChangePass.setOnClickListener(v -> mostrarDialogoCambioPassword());

        // Botón Logout
        btnLogout.setOnClickListener(v -> cerrarSesionTotal());
    }

    private void cerrarSesionTotal() {
        String token = sessionManager.fetchAuthToken();

        // Avisamos al servidor para que borre el Token FCM
        // Si no hay token o ya expiró, cerramos local directamente
        if (token == null) {
            sessionManager.logout();
            irAlLogin();
            return;
        }

        RetrofitClient.getApiService().logoutFCM("Bearer " + token).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                // 2. Independientemente de si el servidor responde o no, cerramos local
                sessionManager.logout();
                irAlLogin();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                // Si falla la red, cerramos igual por seguridad
                sessionManager.logout();
                irAlLogin();
            }
        });
    }

    private void irAlLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void mostrarDialogoCambioPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar Contraseña");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputActual = new EditText(this);
        inputActual.setHint("Contraseña actual");
        inputActual.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputActual);

        final EditText inputNueva = new EditText(this);
        inputNueva.setHint("Nueva contraseña");
        inputNueva.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputNueva);

        builder.setView(layout);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String passActual = inputActual.getText().toString();
            String passNueva = inputNueva.getText().toString();
            if (!passActual.isEmpty() && !passNueva.isEmpty()) {
                ejecutarCambioPassword(passActual, passNueva);
            } else {
                Toast.makeText(ConfigActivity.this, "Campos obligatorios", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void ejecutarCambioPassword(String actual, String nueva) {
        String token = sessionManager.fetchAuthToken();
        Map<String, String> body = new HashMap<>();
        body.put("current_password", actual);
        body.put("new_password", nueva);

        RetrofitClient.getApiService().cambiarContrasena("Bearer " + token, body)
                .enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ConfigActivity.this, "Contraseña actualizada correctamente",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            if (response.code() == 401) {
                                Toast.makeText(ConfigActivity.this, "La contraseña actual no es correcta",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ConfigActivity.this, "Error al actualizar: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        Toast.makeText(ConfigActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}
