package com.example.controldepresencia2026;

import android.Manifest;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.utils.SessionManager;
import com.example.controldepresencia2026.view.LoginActivity;
import com.example.controldepresencia2026.viewmodel.MainViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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

    private TextView tvStatus;
    private Button btnEntrada, btnSalida, btnEnviarIncidencia, btnLogout;
    private EditText etIncidencia;

    private MapView map = null;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar SessionManager y verificar el token
        sessionManager = new SessionManager(this);
        String token = sessionManager.fetchAuthToken();

        if (token == null) {
            irAlLogin();
            return;
        }

        // CONFIGURACIÓN OSMDROID
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        // Configuración visual y carga del Layout
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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
        btnEntrada = findViewById(R.id.btnEntrada);
        btnSalida = findViewById(R.id.btnSalida);
        btnEnviarIncidencia = findViewById(R.id.btnEnviarIncidencia);
        etIncidencia = findViewById(R.id.etIncidencia);
        btnLogout = findViewById(R.id.btnLogout);

        // Inicializar el Mapa
        map = findViewById(R.id.map);
        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
        }

        // Configurar el mensaje de bienvenida
        String nombreUsuario = sessionManager.fetchUserName();
        if (nombreUsuario != null) {
            tvWelcome.setText("Hola, " + nombreUsuario);
        }

        // Inicializar servicios y cargar datos
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.consultarEstado(token);
        configurarObservadores();
        configurarBotones(token);

        // Cargar configuración de la empresa (Ubicación y Radio)
        cargarConfiguracionYMapa(token);

        // Inicializar adaptador NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC", Toast.LENGTH_LONG).show();
        }

        // Crear el PendingIntent para capturar el tag cuando la app esté abierta
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
    }

    private void cargarConfiguracionYMapa(String token) {
        if (map == null) return;

        RetrofitClient.getApiService().obtenerConfigEmpresa("Bearer " + token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        double lat = ((Number) response.body().get("lat")).doubleValue();
                        double lng = ((Number) response.body().get("lng")).doubleValue();
                        double radio = ((Number) response.body().get("radio")).doubleValue();

                        GeoPoint startPoint = new GeoPoint(lat, lng);
                        map.getController().setZoom(17.5);
                        map.getController().setCenter(startPoint);

                        // 1. Añadir Marcador
                        Marker startMarker = new Marker(map);
                        startMarker.setPosition(startPoint);
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        startMarker.setTitle("Sede de la Empresa");
                        map.getOverlays().add(startMarker);

                        // 2. Dibujar el Radio (Círculo)
                        Polygon circle = new Polygon();
                        circle.setPoints(Polygon.pointsAsCircle(startPoint, radio));
                        circle.getFillPaint().setColor(0x220000FF); // Azul transparente
                        circle.getOutlinePaint().setColor(Color.BLUE);
                        circle.getOutlinePaint().setStrokeWidth(2);
                        map.getOverlays().add(circle);

                        map.invalidate(); // Refrescar mapa
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error al cargar mapa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Ciclo de vida del Mapa y NFC
    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            Toast.makeText(this, "NFC Detectado: Procesando fichaje...", Toast.LENGTH_SHORT).show();

            // Obtenemos el token de la sesión
            String token = sessionManager.fetchAuthToken();

            // Lógica: Si está fichado, hacemos salida. Si no, hacemos entrada.
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

        mainViewModel.getMensajeExito().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
        mainViewModel.getError().observe(this, err -> Toast.makeText(this, err, Toast.LENGTH_LONG).show());
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

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            irAlLogin();
        });
    }

    private void obtenerUbicacionYFichar(String token) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                mainViewModel.ficharSalida(token, location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación. Activa el GPS para salir.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irAlLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
