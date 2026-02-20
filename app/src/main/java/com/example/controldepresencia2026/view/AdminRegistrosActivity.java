package com.example.controldepresencia2026.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controldepresencia2026.R;
import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.model.RegistroAdmin;
import com.example.controldepresencia2026.model.TrabajadorSimple;
import com.example.controldepresencia2026.utils.SessionManager;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRegistrosActivity extends AppCompatActivity {
    private android.widget.Spinner spEmpleados;
    private android.widget.TextView tvConfigInfo;
    private RecyclerView recyclerView;
    private SessionManager sessionManager;
    private List<TrabajadorSimple> listaTrabajadores;
    private AdminAdapter adapter;
    private boolean isSpinnerInitial = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_registros);

        recyclerView = findViewById(R.id.rvRegistrosAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        spEmpleados = findViewById(R.id.spEmpleados);

        // Hacemos el try-catch por si el botón ConfigInfo no existiera en el XML
        try {
            tvConfigInfo = findViewById(R.id.tvConfigInfo);
        } catch (Exception e) {
        }

        sessionManager = new SessionManager(this);

        cargarTrabajadores();
        cargarConfiguracionEmpresa();
    }

    private void cargarConfiguracionEmpresa() {
        String token = sessionManager.fetchAuthToken();
        if (token == null || tvConfigInfo == null)
            return;

        RetrofitClient.getApiService().obtenerConfigEmpresa("Bearer " + token)
                .enqueue(new Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<java.util.Map<String, Object>> call,
                            Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String nombre = (String) response.body().get("nombre");
                                double radio = ((Number) response.body().get("radio")).doubleValue();
                                tvConfigInfo.setText("Configuración: " + nombre + " (Radio: " + radio + "m)");
                            } catch (Exception e) {
                                tvConfigInfo.setText("Configuración: Error al leer datos");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                        tvConfigInfo.setText("Configuración: Sin conexión");
                    }
                });
    }

    private void cargarTrabajadores() {
        String token = sessionManager.fetchAuthToken();
        if (token == null)
            return;

        RetrofitClient.getApiService().obtenerTrabajadores("Bearer " + token)
                .enqueue(new Callback<List<TrabajadorSimple>>() {
                    @Override
                    public void onResponse(Call<List<TrabajadorSimple>> call,
                            Response<List<TrabajadorSimple>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            listaTrabajadores = response.body();

                            List<String> nombresSpinner = new ArrayList<>();
                            nombresSpinner.add("Todos los empleados");

                            for (TrabajadorSimple t : listaTrabajadores) {
                                nombresSpinner.add(t.getNombre());
                            }

                            android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
                                    AdminRegistrosActivity.this, android.R.layout.simple_spinner_dropdown_item,
                                    nombresSpinner);
                            spEmpleados.setAdapter(spinnerAdapter);

                            // Forzamos a cargar TODOS la primera vez que entramos a la pantalla
                            cargarRegistros(null);

                            spEmpleados
                                    .setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(android.widget.AdapterView<?> parent,
                                                android.view.View view, int position, long id) {
                                            // Evitamos que salte el filtro cuando el Spinner se llena por primera vez
                                            if (isSpinnerInitial) {
                                                isSpinnerInitial = false;
                                                return;
                                            }

                                            if (listaTrabajadores == null || listaTrabajadores.isEmpty())
                                                return;

                                            if (position == 0) {
                                                cargarRegistros(null);
                                            } else {
                                                TrabajadorSimple seleccionado = listaTrabajadores.get(position - 1);
                                                // Enviamos el ID limpio
                                                String idStr = String.valueOf(seleccionado.getId());
                                                // Mostrar qué ID estamos enviando
                                                Toast.makeText(AdminRegistrosActivity.this,
                                                        "Filtrando por ID: " + idStr, Toast.LENGTH_SHORT).show();
                                                cargarRegistros(idStr);
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TrabajadorSimple>> call, Throwable t) {
                        Toast.makeText(AdminRegistrosActivity.this, "Error cargando empleados", Toast.LENGTH_SHORT)
                                .show();
                        cargarRegistros(null);
                    }
                });
    }

    // Cargamos los registros del empleado seleccionado
    private void cargarRegistros(String idTrabajador) {
        String token = sessionManager.fetchAuthToken();
        if (token == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RetrofitClient.getApiService().obtenerRegistrosAdmin("Bearer " + token, idTrabajador)
                .enqueue(new Callback<List<RegistroAdmin>>() {
                    @Override
                    public void onResponse(Call<List<RegistroAdmin>> call, Response<List<RegistroAdmin>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<RegistroAdmin> registros = response.body();

                            if (registros.isEmpty()) {
                                Toast.makeText(AdminRegistrosActivity.this, "Este empleado no tiene registros",
                                        Toast.LENGTH_SHORT).show();
                            }

                            adapter = new AdminAdapter(registros);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Toast.makeText(AdminRegistrosActivity.this, "Error al obtener datos", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<RegistroAdmin>> call, Throwable t) {
                        Toast.makeText(AdminRegistrosActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}