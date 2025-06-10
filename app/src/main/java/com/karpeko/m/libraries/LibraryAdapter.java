package com.karpeko.m.libraries;

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

import com.karpeko.m.R;

// Адаптер для списка кафе
public class LibraryAdapter extends ListAdapter<Library, LibraryAdapter.ViewHolder> {
    private final Location userLocation;
    private Library selectedLibrary;

    static final DiffUtil.ItemCallback<Library> DIFF_CALLBACK = new DiffUtil.ItemCallback<Library>() {
        @Override
        public boolean areItemsTheSame(@NonNull Library oldItem, @NonNull Library newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Library oldItem, @NonNull Library newItem) {
            return oldItem.equals(newItem);
        }
    };

    public LibraryAdapter(Location userLocation) {
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
        Library library = getItem(position);
        holder.bind(library);
        holder.itemView.setSelected(library.equals(selectedLibrary));
    }

    public Library getSelectedCafe() {
        return selectedLibrary;
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
                selectedLibrary = getItem(getAdapterPosition());
                notifyDataSetChanged();
            });
        }

        void bind(Library library) {
            tvName.setText(library.getName());

            if (userLocation != null) {
                float[] results = new float[1];
                Location.distanceBetween(
                        userLocation.getLatitude(),
                        userLocation.getLongitude(),
                        library.getLatitude(),
                        library.getLongitude(),
                        results
                );
                tvDistance.setText(String.format("%.1f км", results[0] / 1000));
            }
        }
    }
}
