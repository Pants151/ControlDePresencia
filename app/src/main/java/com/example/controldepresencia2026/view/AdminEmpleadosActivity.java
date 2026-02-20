package com.example.controldepresencia2026.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.controldepresencia2026.R;
import com.example.controldepresencia2026.data.RetrofitClient;
import com.example.controldepresencia2026.model.TrabajadorSimple;
import com.example.controldepresencia2026.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminEmpleadosActivity extends AppCompatActivity {

    private ListView lvEmpleados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_empleados);

        lvEmpleados = findViewById(R.id.lvEmpleados);
        String token = new SessionManager(this).fetchAuthToken();

        cargarListado(token);
    }

    private void cargarListado(String token) {
        RetrofitClient.getApiService().getTrabajadores("Bearer " + token)
                .enqueue(new Callback<List<TrabajadorSimple>>() {
                    @Override
                    public void onResponse(Call<List<TrabajadorSimple>> call,
                            Response<List<TrabajadorSimple>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<String> listaNombres = new ArrayList<>();

                            // Extraemos el nombre e email para mostrarlos en la lista
                            for (TrabajadorSimple trabajador : response.body()) {
                                listaNombres.add("👤 " + trabajador.getNombre() + "\n✉️ " + trabajador.getEmail());
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    AdminEmpleadosActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    listaNombres);
                            lvEmpleados.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TrabajadorSimple>> call, Throwable t) {
                        Toast.makeText(AdminEmpleadosActivity.this, "Fallo al conectar con el servidor",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
