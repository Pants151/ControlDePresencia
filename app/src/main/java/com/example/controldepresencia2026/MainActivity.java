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
    private Button btnEntrada, btnSalida, btnEnviarIncidencia, btnLogout, btnChangePassword;
    private EditText etIncidencia;

    private MapView map = null;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar SessionManager y verificar el token
        sessionManager = new SessionManager(this);
        String token = sessionManager.fetchAuthToken();
        String rol = sessionManager.fetchUserRol();
        // DEBUG: Mostrar el rol actual para ver por qué no sale el botón
        Toast.makeText(this, "Rol detectado: " + rol, Toast.LENGTH_LONG).show();

        Button btnAdmin = findViewById(R.id.btnAdmin);

        if (token == null) {
            irAlLogin();
            return;
        }

        if ("Administrador".equals(rol) || "Superadministrador".equals(rol)) {
            btnAdmin.setVisibility(View.VISIBLE);
        } else {
            btnAdmin.setVisibility(View.GONE);
        }

        btnAdmin.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminActivity.class);
            startActivity(i);
        });

        // CONFIGURACIÓN OSMDROID
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

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
        btnLogout = findViewById(R.id.btnLogout);
        btnChangePassword = findViewById(R.id.btnChangePassword);

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
        mainViewModel.consultarResumen(token);
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
        if (map == null)
            return;

        RetrofitClient.getApiService().obtenerConfigEmpresa("Bearer " + token)
                .enqueue(new Callback<Map<String, Object>>() {
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

                                // Añadir Marcador
                                Marker startMarker = new Marker(map);
                                startMarker.setPosition(startPoint);
                                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                startMarker.setTitle("Sede de la Empresa");
                                map.getOverlays().add(startMarker);

                                // Dibujar el Radio (Círculo)
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
        if (map != null)
            map.onResume();

        // El if es vital para que el emulador no explote al no tener NFC
        if (nfcAdapter != null && pendingIntent != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null)
            map.onPause();

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
                String trabajadoStr = formatearHoras(resumen.getHorasTrabajadas());
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

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            irAlLogin();
        });

        btnChangePassword.setOnClickListener(v -> mostrarDialogoCambioPassword());
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
                Toast.makeText(MainActivity.this, "Campos obligatorios", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(MainActivity.this, "Contraseña actualizada correctamente", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            if (response.code() == 401) {
                                Toast.makeText(MainActivity.this, "La contraseña actual no es correcta",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Error al actualizar: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
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
}
