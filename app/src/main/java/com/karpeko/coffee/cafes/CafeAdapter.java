package com.karpeko.coffee.cafes;

import android.annotation.SuppressLint;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.karpeko.coffee.R;

// Адаптер для списка кафе
public class CafeAdapter extends ListAdapter<Cafe, CafeAdapter.ViewHolder> {
    private final Location userLocation;
    private Cafe selectedCafe;

    static final DiffUtil.ItemCallback<Cafe> DIFF_CALLBACK = new DiffUtil.ItemCallback<Cafe>() {
        @Override
        public boolean areItemsTheSame(@NonNull Cafe oldItem, @NonNull Cafe newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Cafe oldItem, @NonNull Cafe newItem) {
            return oldItem.equals(newItem);
        }
    };

    public CafeAdapter(Location userLocation) {
        super(DIFF_CALLBACK);
        this.userLocation = userLocation;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cafe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cafe cafe = getItem(position);
        holder.bind(cafe);
        holder.itemView.setSelected(cafe.equals(selectedCafe));
    }

    public Cafe getSelectedCafe() {
        return selectedCafe;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        // Элементы макета кафе
        TextView tvName, tvDistance;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_cafe_name);
            tvDistance = itemView.findViewById(R.id.tv_distance);

            itemView.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "Выбрано: " + tvName.getText().toString(), Toast.LENGTH_SHORT).show();
                selectedCafe = getItem(getAdapterPosition());
                notifyDataSetChanged();
            });
        }

        void bind(Cafe cafe) {
            tvName.setText(cafe.getName());

            if (userLocation != null) {
                float[] results = new float[1];
                Location.distanceBetween(
                        userLocation.getLatitude(),
                        userLocation.getLongitude(),
                        cafe.getLatitude(),
                        cafe.getLongitude(),
                        results
                );
                tvDistance.setText(String.format("%.1f км", results[0] / 1000));
            }
        }
    }
}
