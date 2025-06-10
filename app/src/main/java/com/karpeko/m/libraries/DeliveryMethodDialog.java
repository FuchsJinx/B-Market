package com.karpeko.m.libraries;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.m.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeliveryMethodDialog extends DialogFragment {
    private OnDeliveryMethodSelectedListener listener;
    private List<Library> libraries = new ArrayList<>();
    private Location userLocation;
    private String selectedAddress;
    private LatLng selectedLatLng;
    private TextView tvSelectedAddress;

    public interface OnDeliveryMethodSelectedListener {
        void onDeliverySelected(String address, LatLng location);
        void onPickupSelected(Library selectedLibrary);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof OnDeliveryMethodSelectedListener) {
            listener = (OnDeliveryMethodSelectedListener) getTargetFragment();
        } else {
            throw new ClassCastException("Target fragment must implement OnDeliveryMethodSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delivery_method, null);

        RadioGroup rgDeliveryType = view.findViewById(R.id.rg_delivery_type);
        LinearLayout pickupContainer = view.findViewById(R.id.pickup_container);
        LinearLayout deliveryContainer = view.findViewById(R.id.delivery_container);
        RecyclerView rvCafes = view.findViewById(R.id.rv_cafes);
        LibraryAdapter adapter = new LibraryAdapter(userLocation);
        tvSelectedAddress = view.findViewById(R.id.tv_selected_address);
        Button btnSelectAddress = view.findViewById(R.id.btn_select_address);

        btnSelectAddress.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corners));
        btnSelectAddress.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.purple_700));

        rvCafes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCafes.setAdapter(adapter);

        ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedAddress = data.getStringExtra("address");
                        double lat = data.getDoubleExtra("lat", 0);
                        double lng = data.getDoubleExtra("lng", 0);
                        selectedLatLng = new LatLng(lat, lng);
                        tvSelectedAddress.setVisibility(View.VISIBLE);
                        tvSelectedAddress.setText(selectedAddress);
                    }
                });

        btnSelectAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapActivity.class);
            mapLauncher.launch(intent);
        });

        rgDeliveryType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_pickup) {
                fetchCafes(adapter);
                pickupContainer.setVisibility(View.VISIBLE);
                deliveryContainer.setVisibility(View.GONE);
            } else {
                pickupContainer.setVisibility(View.GONE);
                deliveryContainer.setVisibility(View.VISIBLE);
            }
        });

        builder.setView(view)
                .setPositiveButton("Подтвердить", (dialog, id) -> {
                    int selectedId = rgDeliveryType.getCheckedRadioButtonId();
                    if (selectedId == R.id.rb_pickup) {
                        if (adapter.getSelectedCafe() != null) {
                            listener.onPickupSelected(adapter.getSelectedCafe());
                        } else {
                            Toast.makeText(getContext(), "Выберите книжный магазин для самовывоза", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (selectedAddress != null && selectedLatLng != null) {
                            listener.onDeliverySelected(selectedAddress, selectedLatLng);
                        } else {
                            Toast.makeText(getContext(), "Выберите адрес доставки на карте", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_700));
            }
        });

        return dialog;
    }


    private void fetchCafes(LibraryAdapter adapter) {
        FirebaseFirestore.getInstance().collection("libraries")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    libraries = queryDocumentSnapshots.toObjects(Library.class);
                    sortCafesByDistance();
                    adapter.submitList(libraries);
                });
    }

    private void sortCafesByDistance() {
        if (userLocation == null || libraries == null || libraries.isEmpty()) return;

        final Location userLoc = new Location("user");
        userLoc.setLatitude(userLocation.getLatitude());
        userLoc.setLongitude(userLocation.getLongitude());

        List<Pair<Library, Float>> cafeDistances = new ArrayList<>();
        for (Library library : libraries) {
            if (library.getLatitude() == 0 && library.getLongitude() == 0) continue;
            Location cafeLoc = new Location("library");
            cafeLoc.setLatitude(library.getLatitude());
            cafeLoc.setLongitude(library.getLongitude());
            cafeDistances.add(new Pair<>(library, userLoc.distanceTo(cafeLoc)));
        }

        Collections.sort(cafeDistances, (p1, p2) -> Float.compare(p1.second, p2.second));

        libraries.clear();
        for (Pair<Library, Float> pair : cafeDistances) {
            libraries.add(pair.first);
        }
    }

    public void setUserLocation(Location location) {
        this.userLocation = location;
    }
}
