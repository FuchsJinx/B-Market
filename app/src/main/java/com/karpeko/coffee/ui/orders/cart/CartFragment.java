package com.karpeko.coffee.ui.orders.cart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karpeko.coffee.R;
import com.karpeko.coffee.account.UserSessionManager;
import com.karpeko.coffee.cafes.Cafe;
import com.karpeko.coffee.cafes.DeliveryMethodDialog;
import com.karpeko.coffee.notification.OrderNotificationHelper;
import com.karpeko.coffee.ui.menu.lists.item.ItemEditActivity;
import com.karpeko.coffee.ui.orders.OrderWorkHelper;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements
        DeliveryMethodDialog.OnDeliveryMethodSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private Button buttonOrder;
    private CartWorkHelper cartWorkHelper;
    private UserSessionManager userSessionManager;
    private TextView textTotal;
    private List<CartItem> cartItemsInDatabase = new ArrayList<>();
    private double total = 0;
    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        userSessionManager = new UserSessionManager(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        cartWorkHelper = new CartWorkHelper();
        textTotal = view.findViewById(R.id.textTotal);
        buttonOrder = view.findViewById(R.id.buttonOrder);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CartAdapter(new ArrayList<>(), null);
        recyclerView.setAdapter(adapter);

        loadCartItemsFromFirestore();
        buttonOrder.setOnClickListener(v -> checkLocationPermission());

        return view;
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(getContext(), "Для оформления заказа требуется доступ к геолокации", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        if (cartItemsInDatabase.size() == 0) {
                            Toast.makeText(getContext(), "Добавьте товары в корзину", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showDeliveryDialog(location);
                    } else {
                        Toast.makeText(getContext(), "Не удалось определить местоположение", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeliveryDialog(Location userLocation) {
        DeliveryMethodDialog dialog = new DeliveryMethodDialog();
        dialog.setUserLocation(userLocation);
        dialog.setTargetFragment(this, 1);
        dialog.show(getParentFragmentManager(), "DeliveryMethodDialog");
    }

    private void loadCartItemsFromFirestore() {
        String userId = userSessionManager.getUserId();
        if (userId == null) return;

        db.collection("carts")
                .document(userId)
                .collection("cart_items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Listen failed.", error);
                        return;
                    }

                    List<CartItem> cartItems = new ArrayList<>();
                    double total = 0;
                    for (QueryDocumentSnapshot document : value) {
                        CartItem cartItem = document.toObject(CartItem.class);
                        cartItem.setCartItemId(document.getId());
                        cartItems.add(cartItem);
                        total += cartItem.getPrice() * cartItem.quantity;
                    }
                    adapter.setCartItems(cartItems);
                    textTotal.setText("Итого: " + ((int) total) + " ₽");
                    cartItemsInDatabase = cartItems;
                    this.total = total;
                });
    }

    @Override
    public void onDeliverySelected(String address, LatLng location) {
        createOrderWithDeliveryType("delivery", address, location, null);
    }

    @Override
    public void onPickupSelected(Cafe selectedCafe) {
        createOrderWithDeliveryType("pickup", null, null, selectedCafe.getName());
    }

    private void createOrderWithDeliveryType(String deliveryType,
                                             String address,
                                             LatLng deliveryLocation,
                                             String cafeId) {

        String userId = userSessionManager.getUserId();
        if (userId == null) return;

        OrderWorkHelper orderHelper = new OrderWorkHelper();
        orderHelper.createOrder(
                userId,
                total,
                cartItemsInDatabase,
                deliveryType,
                address,
                deliveryLocation != null ? deliveryLocation.latitude : 0,
                deliveryLocation != null ? deliveryLocation.longitude : 0,
                cafeId,
                orderId -> {
                    new OrderNotificationHelper(requireContext())
                            .showOrderCreatedNotification(orderId, total, cartItemsInDatabase.size());

                    Toast.makeText(getContext(), "Заказ оформлен! Номер: " + orderId, Toast.LENGTH_LONG).show();
                    playSound(R.raw.right_answer);

                    cartWorkHelper.clearCart(userId, new CartWorkHelper.OnCartClearedListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onCartCleared() {
                            adapter.setCartItems(new ArrayList<>());
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("CartClear", "Ошибка очистки", e);
                        }
                    });
                });
    }

//    @Override
//    public void onItemClick(CartItem item) {
//        Intent intent = new Intent(getContext(), ItemEditActivity.class);
//        intent.putExtra("itemId", item.getItemId());
//        intent.putExtra("cartItemId", item.getCartItemId());
//        intent.putExtra("price", item.getPrice());
//        intent.putExtra("quantity", item.getQuantity());
//        startActivity(intent);
//    }

    private void playSound(int resId) {
        MediaPlayer click = MediaPlayer.create(getContext(), resId);
        click.setVolume(0.5f, 0.5f);
        click.start();
    }
}
