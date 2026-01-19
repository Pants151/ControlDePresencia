package com.example.controldepresencia2026;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.controldepresencia2026.utils.SessionManager;
import com.example.controldepresencia2026.view.LoginActivity;
import com.example.controldepresencia2026.viewmodel.MainViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private SessionManager sessionManager;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvStatus;
    private Button btnEntrada, btnSalida, btnEnviarIncidencia, btnLogout;
    private EditText etIncidencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Verificación de Seguridad: Sesión activa
        sessionManager = new SessionManager(this);
        String token = sessionManager.fetchAuthToken();

        if (token == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2. Configuración de Interfaz y EdgeToEdge
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Aplicar padding para barras de sistema (Insets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 3. Inicializar Vistas
        tvStatus = findViewById(R.id.tvStatus);
        btnEntrada = findViewById(R.id.btnEntrada);
        btnSalida = findViewById(R.id.btnSalida);
        btnEnviarIncidencia = findViewById(R.id.btnEnviarIncidencia);
        etIncidencia = findViewById(R.id.etIncidencia);
        btnLogout = new Button(this); // (Opcional) Puedes añadirlo a tu XML

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 4. Consultar estado inicial del trabajador
        mainViewModel.consultarEstado(token);

        // 5. Observadores (Arquitectura MVVM)
        mainViewModel.getEstado().observe(this, estado -> {
            if (estado.isFichado()) {
                tvStatus.setText("Estado: TRABAJANDO (Desde: " + estado.getUltimaEntrada() + ")");
                btnEntrada.setVisibility(View.GONE);
                btnSalida.setVisibility(View.VISIBLE);
            } else {
                tvStatus.setText("Estado: FUERA DE SERVICIO");
                btnEntrada.setVisibility(View.VISIBLE);
                btnSalida.setVisibility(View.GONE);
            }
        });

        mainViewModel.getMensajeExito().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
        mainViewModel.getError().observe(this, err -> Toast.makeText(this, err, Toast.LENGTH_LONG).show());

        // 6. Eventos de botones
        btnEntrada.setOnClickListener(v -> obtenerUbicacionYFichar(token));

        btnSalida.setOnClickListener(v -> mainViewModel.ficharSalida(token));

        btnEnviarIncidencia.setOnClickListener(v -> {
            String desc = etIncidencia.getText().toString();
            if (!desc.isEmpty()) {
                mainViewModel.enviarIncidencia(token, desc);
                etIncidencia.setText("");
            }
        });
    }

    private void obtenerUbicacionYFichar(String token) {
        // Comprobar permisos GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                mainViewModel.ficharEntrada(token, location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación. Activa el GPS.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}