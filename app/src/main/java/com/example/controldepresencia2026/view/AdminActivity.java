package com.example.controldepresencia2026.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controldepresencia2026.R;
import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.model.RegistroAdmin;
import com.example.controldepresencia2026.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {
    private android.widget.Spinner spEmpleados;
    private RecyclerView recyclerView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerView = findViewById(R.id.rvRegistrosAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        spEmpleados = findViewById(R.id.spEmpleados);
        sessionManager = new SessionManager(this);

        cargarTrabajadores();
    }

    private void cargarTrabajadores() {
        String token = sessionManager.fetchAuthToken();
        if (token == null)
            return;

        RetrofitClient.getApiService().obtenerTrabajadores("Bearer " + token)
                .enqueue(new Callback<List<com.example.controldepresencia2026.model.TrabajadorSimple>>() {
                    @Override
                    public void onResponse(Call<List<com.example.controldepresencia2026.model.TrabajadorSimple>> call,
                            Response<List<com.example.controldepresencia2026.model.TrabajadorSimple>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<com.example.controldepresencia2026.model.TrabajadorSimple> lista = response.body();
                            // Añadir opción "Todos" al principio
                            lista.add(0, new com.example.controldepresencia2026.model.TrabajadorSimple(-1, "Todos"));

                            android.widget.ArrayAdapter<com.example.controldepresencia2026.model.TrabajadorSimple> adapter = new android.widget.ArrayAdapter<>(
                                    AdminActivity.this, android.R.layout.simple_spinner_dropdown_item, lista);
                            spEmpleados.setAdapter(adapter);

                            // Listener para filtrar al seleccionar
                            spEmpleados
                                    .setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(android.widget.AdapterView<?> parent,
                                                android.view.View view, int position, long id) {
                                            com.example.controldepresencia2026.model.TrabajadorSimple seleccionado = (com.example.controldepresencia2026.model.TrabajadorSimple) parent
                                                    .getItemAtPosition(position);
                                            if (seleccionado.getId() == -1) {
                                                cargarRegistros(null); // Todos
                                            } else {
                                                cargarRegistros(String.valueOf(seleccionado.getId()));
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                                            cargarRegistros(null);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<com.example.controldepresencia2026.model.TrabajadorSimple>> call,
                            Throwable t) {
                        Toast.makeText(AdminActivity.this, "Error cargando empleados", Toast.LENGTH_SHORT).show();
                        cargarRegistros(null); // Cargar todos por defecto si falla la lista
                    }
                });
    }

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
                            AdminAdapter adapter = new AdminAdapter(response.body());
                            recyclerView.setAdapter(adapter);
                        } else {
                            // Si el usuario no ha actualizado el backend, fallará aquí.
                            Toast.makeText(AdminActivity.this, "Error obteniendo registros: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<RegistroAdmin>> call, Throwable t) {
                        Toast.makeText(AdminActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}