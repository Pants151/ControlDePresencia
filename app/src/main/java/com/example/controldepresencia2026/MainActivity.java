package com.example.controldepresencia2026;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.text.InputType;
import java.util.HashMap;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.model.BasicResponse;
import com.example.controldepresencia2026.utils.SessionManager;
import com.example.controldepresencia2026.view.AdminActivity;
import com.example.controldepresencia2026.view.LoginActivity;
import com.example.controldepresencia2026.viewmodel.MainViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.messaging.FirebaseMessaging;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private SessionManager sessionManager;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvStatus, tvResumen;
    private Button btnEntrada, btnSalida, btnEnviarIncidencia, btnConfig;
    private EditText etIncidencia;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Crear canal de notificaciones
        createNotificationChannel();

        // Pedir permiso de notificaciones en Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.POST_NOTIFICATIONS }, 101);
            }
        }

        // Inicializar SessionManager y verificar el token
        sessionManager = new SessionManager(this);
        String token = sessionManager.fetchAuthToken();
        String rol = sessionManager.fetchUserRol();

        // CONFIGURACIÓN OSMDROID NO NECESARIA AQUI YA
        // Configuration.getInstance().load(...) se movió a ConfigActivity

        // Ajustar padding para barras de sistema

        // Ajustar padding para barras de sistema
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inicialización de componentes
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvStatus = findViewById(R.id.tvStatus);
        tvResumen = findViewById(R.id.tvResumen);
        btnEntrada = findViewById(R.id.btnEntrada);
        btnSalida = findViewById(R.id.btnSalida);
        btnEnviarIncidencia = findViewById(R.id.btnEnviarIncidencia);
        etIncidencia = findViewById(R.id.etIncidencia);
        btnConfig = findViewById(R.id.btnConfig);

        // Configuración botón configuración
        btnConfig.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.controldepresencia2026.view.ConfigActivity.class));
        });

        // Configurar el mensaje de bienvenida
        String nombreUsuario = sessionManager.fetchUserName();
        if (nombreUsuario != null) {
            tvWelcome.setText("Hola, " + nombreUsuario);
        }

        // Inicializar servicios y cargar datos
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.consultarEstado(token);
        mainViewModel.consultarResumen(token);
        configurarObservadores();
        configurarBotones(token);

        // Inicializar adaptador NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC", Toast.LENGTH_LONG).show();
        }

        // Crear el PendingIntent para capturar el tag cuando la app esté abierta
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // ACTUALIZAR TOKEN FCM
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    return;
                }
                String fcmToken = task.getResult();
                Map<String, String> body = new HashMap<>();
                body.put("fcm_token", fcmToken);

                RetrofitClient.getApiService().actualizarTokenFCM("Bearer " + token, body)
                        .enqueue(new Callback<BasicResponse>() {
                            @Override
                            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                                // No hace falta molestar al usuario si sale bien
                            }

                            @Override
                            public void onFailure(Call<BasicResponse> call, Throwable t) {
                            }
                        });
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Evitar crash si Firebase no está bien configurado
        }
    }

    // Ciclo de vida del Mapa y NFC
    @Override
    public void onResume() {
        super.onResume();
        // El if es vital para que el emulador no explote al no tener NFC
        if (nfcAdapter != null && pendingIntent != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            Toast.makeText(this, "Tarjeta NFC detectada. Procesando fichaje...", Toast.LENGTH_SHORT).show();

            // Obtenemos el token de la sesión
            String token = sessionManager.fetchAuthToken();

            // Si está fichado, hacemos salida. Si no, hacemos entrada.
            if (mainViewModel.getEstado().getValue() != null && mainViewModel.getEstado().getValue().isFichado()) {
                obtenerUbicacionYFicharSalida(token);
            } else {
                obtenerUbicacionYFichar(token);
            }
        }
    }

    private void configurarObservadores() {
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

        // OBSERVADOR PARA EL RESUMEN
        mainViewModel.getResumenMensual().observe(this, resumen -> {
            if (resumen != null) {
                String trabajadoStr = formatearHoras(resumen.getHorasReales());
                String teoricoStr = formatearHoras(resumen.getHorasTeoricas());
                String extrasStr = formatearHoras(resumen.getHorasExtra());

                String texto = String.format("Mes: %s\nTrabajado: %s\nTeórico: %s\nExtras: %s",
                        resumen.getMes(),
                        trabajadoStr,
                        teoricoStr,
                        extrasStr);

                tvResumen.setText(texto);
            }
        });

        mainViewModel.getMensajeExito().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
        mainViewModel.getError().observe(this, err -> {
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
            if (err.contains("401") || err.contains("No autorizado")) {
                sessionManager.logout();
                irAlLogin();
            }
        });
    }

    private void configurarBotones(String token) {
        btnEntrada.setOnClickListener(v -> obtenerUbicacionYFichar(token));
        btnSalida.setOnClickListener(v -> obtenerUbicacionYFicharSalida(token));

        btnEnviarIncidencia.setOnClickListener(v -> {
            String desc = etIncidencia.getText().toString().trim();
            if (!desc.isEmpty()) {
                mainViewModel.enviarIncidencia(token, desc);
                etIncidencia.setText("");
            }
        });
    }

    private void obtenerUbicacionYFichar(String token) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                mainViewModel.ficharEntrada(token, location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación. Activa el GPS.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void obtenerUbicacionYFicharSalida(String token) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                mainViewModel.ficharSalida(token, location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación. Activa el GPS para salir.", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void irAlLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private String formatearHoras(double horasDecimales) {
        int horas = (int) horasDecimales;
        int minutos = (int) Math.round((horasDecimales - horas) * 60);

        // Si los minutos redondean a 60, ajustamos las horas
        if (minutos == 60) {
            horas++;
            minutos = 0;
        }

        return horas + "h " + minutos + "min";
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Recordatorios de Fichaje";
            String description = "Canal para avisos de olvido de fichaje";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("CANAL_PRESENCIA", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
