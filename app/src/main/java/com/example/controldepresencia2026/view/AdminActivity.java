package com.example.controldepresencia2026.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.controldepresencia2026.R;
import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.model.BasicResponse;
import com.example.controldepresencia2026.utils.SessionManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private MapView map = null;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_admin);

        token = new SessionManager(this).fetchAuthToken();
        map = findViewById(R.id.mapAdmin);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        EditText etRadio = findViewById(R.id.etRadioAdmin);
        Button btnGuardarRadio = findViewById(R.id.btnGuardarRadioAdmin);
        Button btnVerEmpleados = findViewById(R.id.btnVerEmpleados);
        Button btnVerRegistros = findViewById(R.id.btnVerRegistros);

        cargarConfiguracionYMapa();

        // 1. Guardar el nuevo radio
        btnGuardarRadio.setOnClickListener(v -> {
            String nuevoRadio = etRadio.getText().toString();
            if (nuevoRadio.isEmpty())
                return;

            Map<String, Object> body = new HashMap<>();
            body.put("radio", Double.parseDouble(nuevoRadio));

            RetrofitClient.getApiService().actualizarConfigEmpresa("Bearer " + token, body)
                    .enqueue(new Callback<BasicResponse>() {
                        @Override
                        public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminActivity.this, "Radio actualizado correctamente",
                                        Toast.LENGTH_SHORT).show();
                                cargarConfiguracionYMapa(); // Recargamos para dibujar el nuevo círculo
                            }
                        }

                        @Override
                        public void onFailure(Call<BasicResponse> call, Throwable t) {
                            Toast.makeText(AdminActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 2. Navegación a las otras pantallas
        btnVerEmpleados.setOnClickListener(v -> startActivity(new Intent(this, AdminEmpleadosActivity.class)));
        // Este es el que has renombrado en el Paso 1
        btnVerRegistros.setOnClickListener(v -> startActivity(new Intent(this, AdminRegistrosActivity.class)));
    }

    private void cargarConfiguracionYMapa() {
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

                                map.getOverlays().clear(); // Limpiamos marcadores viejos

                                Marker startMarker = new Marker(map);
                                startMarker.setPosition(startPoint);
                                startMarker.setTitle("Sede Empresa");
                                map.getOverlays().add(startMarker);

                                Polygon circle = new Polygon();
                                circle.setPoints(Polygon.pointsAsCircle(startPoint, radio));
                                circle.getFillPaint().setColor(0x220000FF);
                                circle.getOutlinePaint().setColor(android.graphics.Color.BLUE);
                                circle.getOutlinePaint().setStrokeWidth(2);
                                map.getOverlays().add(circle);

                                map.invalidate(); // Refrescar mapa

                                // Ponemos el radio actual en el cuadro de texto
                                ((EditText) findViewById(R.id.etRadioAdmin)).setText(String.valueOf(radio));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    }
                });
    }
}
