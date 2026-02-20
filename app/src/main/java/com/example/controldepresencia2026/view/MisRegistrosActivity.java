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

public class MisRegistrosActivity extends AppCompatActivity {

    private RecyclerView rvMisRegistros;
    private AdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_registros);

        rvMisRegistros = findViewById(R.id.rvMisRegistros);
        rvMisRegistros.setLayoutManager(new LinearLayoutManager(this));

        String token = new SessionManager(this).fetchAuthToken();
        cargarMisRegistros(token);
    }

    private void cargarMisRegistros(String token) {
        RetrofitClient.getApiService().getMisRegistros("Bearer " + token)
                .enqueue(new Callback<List<RegistroAdmin>>() {
                    @Override
                    public void onResponse(Call<List<RegistroAdmin>> call, Response<List<RegistroAdmin>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter = new AdminAdapter(response.body());
                            rvMisRegistros.setAdapter(adapter);
                        } else {
                            Toast.makeText(MisRegistrosActivity.this, "No se encontraron registros", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<RegistroAdmin>> call, Throwable t) {
                        Toast.makeText(MisRegistrosActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
