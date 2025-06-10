package com.karpeko.m.account;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;
import com.karpeko.m.MainActivity;
import com.karpeko.m.R;
import com.karpeko.m.notification.OrderNotificationHelper;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 3000; // 3 секунды задержки
    private boolean isNextScreenStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        startService(new Intent(this, BackgroundSoundService.class));

        OrderNotificationHelper notificationHelper = new OrderNotificationHelper(this);
        notificationHelper.scheduleDailyNotifications();

        WorkManager.getInstance(this)
                .getWorkInfosForUniqueWorkLiveData("orderCheckWork")
                .observe(this, workInfos -> {
                    Log.d("WORKER", "Статус Worker: " +
                            (workInfos.isEmpty() ? "не найден" : workInfos.get(0).getState()));
                });

        LottieAnimationView animationView = findViewById(R.id.lottieAnimationView);
        animationView.setAnimation(R.raw.start_animation);
        animationView.setRenderMode(RenderMode.HARDWARE);

        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Можно добавить логику при старте анимации
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                proceedNextScreen();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                proceedNextScreen();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Не используется
            }
        });

        // Запасной таймер на случай, если анимация не сработает
        new Handler().postDelayed(this::proceedNextScreen, SPLASH_DELAY);
    }

    private void proceedNextScreen() {
        if (isNextScreenStarted) return;
        isNextScreenStarted = true;

        SharedPreferences preferences = getSharedPreferences("accountPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, RegistrationActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
