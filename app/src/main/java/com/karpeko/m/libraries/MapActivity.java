package com.karpeko.m.libraries;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.karpeko.m.R;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.mapview.MapView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private Point selectedPoint;
    private TextView tvAddress;
    private boolean isAddressValid = false;
    private static final String TARGET_CITY = "Брянск";

    // Примерные границы Брянска (уточните при необходимости)
    private static final double MIN_LAT = 53.1;
    private static final double MAX_LAT = 53.4;
    private static final double MIN_LNG = 34.1;
    private static final double MAX_LNG = 34.6;

    // Слушатель теперь поле класса!
    private InputListener inputListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapview);
        tvAddress = findViewById(R.id.tv_address);

        // Центрируем карту на Брянске
        mapView.getMap().move(
                new CameraPosition(new Point(53.252090, 34.371917), 12.0f, 0.0f, 0.0f)
        );

        // Создаём и сохраняем слушатель в поле класса
        inputListener = new InputListener() {
            @Override
            public void onMapTap(Map map, Point point) {
                selectedPoint = point;
                updateAddress(point);
            }

            @Override
            public void onMapLongTap(Map map, Point point) {
                // Не используется
            }
        };
        mapView.getMap().addInputListener(inputListener);

        findViewById(R.id.btn_confirm).setOnClickListener(v -> returnResult());
    }

    private void updateAddress(Point point) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    point.getLatitude(),
                    point.getLongitude(),
                    1
            );

            if (!addresses.isEmpty() && !addresses.get(0).getAddressLine(0).isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);

                // Проверка города и координат
                boolean isCityValid = checkCity(address) && checkCoordinates(point);

                if (isCityValid) {
                    isAddressValid = true;
                    tvAddress.setTextColor(ContextCompat.getColor(this, R.color.green));
                    tvAddress.setText(fullAddress);
                } else {
                    isAddressValid = false;
                    tvAddress.setTextColor(ContextCompat.getColor(this, R.color.red));
                    tvAddress.setText("Доставка возможна только в Брянске!");
                    selectedPoint = null;
                }
            }
        } catch (IOException e) {
            tvAddress.setTextColor(Color.RED);
            tvAddress.setText("Ошибка определения адреса");
            e.printStackTrace();
        }
    }

    private boolean checkCity(Address address) {
        // Проверка через официальное название города
        if (address.getLocality() != null &&
                address.getLocality().equalsIgnoreCase(TARGET_CITY)) {
            return true;
        }

        // Проверка в адресной строке
        String addressLine = address.getAddressLine(0);
        return addressLine != null &&
                addressLine.toLowerCase().contains(TARGET_CITY.toLowerCase());
    }

    private boolean checkCoordinates(Point point) {
        // Проверка географических границ
        return point.getLatitude() >= MIN_LAT &&
                point.getLatitude() <= MAX_LAT &&
                point.getLongitude() >= MIN_LNG &&
                point.getLongitude() <= MAX_LNG;
    }

    private void returnResult() {
        if (selectedPoint != null && isAddressValid) {
            Intent result = new Intent();
            result.putExtra("address", tvAddress.getText().toString());
            result.putExtra("lat", selectedPoint.getLatitude());
            result.putExtra("lng", selectedPoint.getLongitude());
            setResult(RESULT_OK, result);
            finish();
        } else {
            Toast.makeText(this,
                    "Доставка возможна только в пределах Брянска",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}
