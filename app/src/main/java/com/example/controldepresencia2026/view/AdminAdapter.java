package com.example.controldepresencia2026.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controldepresencia2026.R;
import com.example.controldepresencia2026.model.RegistroAdmin;

import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.ViewHolder> {
    private List<RegistroAdmin> registros;

    public AdminAdapter(List<RegistroAdmin> registros) {
        this.registros = registros;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registro_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistroAdmin registro = registros.get(position);
        holder.tvEmpleado.setText(registro.getEmpleado());
        holder.tvEntrada.setText(registro.getEntrada());
        holder.tvSalida.setText(registro.getSalida());
        holder.tvTotal.setText(registro.getTotal());
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmpleado, tvEntrada, tvSalida, tvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmpleado = itemView.findViewById(R.id.tvEmpleado);
            tvEntrada = itemView.findViewById(R.id.tvEntrada);
            tvSalida = itemView.findViewById(R.id.tvSalida);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
}
