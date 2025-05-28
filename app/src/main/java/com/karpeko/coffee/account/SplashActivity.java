package com.karpeko.coffee.account;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import com.karpeko.coffee.MainActivity;
import com.karpeko.coffee.R;
import com.karpeko.coffee.notification.OrderNotificationHelper;
import com.karpeko.coffee.ui.orders.history.OrderWorkScheduler;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        OrderNotificationHelper notificationHelper = new OrderNotificationHelper(this);
        notificationHelper.scheduleDailyNotifications();

        // Проверка статуса
        WorkManager.getInstance(this)
                .getWorkInfosForUniqueWorkLiveData("orderCheckWork")
                .observe(this, workInfos -> {
                    Log.d("WORKER", "Статус Worker: " +
                            (workInfos.isEmpty() ? "не найден" : workInfos.get(0).getState()));
                });

        SharedPreferences preferences = getSharedPreferences("accountPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, RegistrationActivity.class));
        }

        finish();
    }
}