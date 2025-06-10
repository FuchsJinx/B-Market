package com.karpeko.m.ui.account;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.karpeko.m.R;

import com.airbnb.lottie.LottieAnimationView;

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation(R.raw.infinity_animation);

        // Запускаем анимацию (если не включено в XML)
        lottieAnimationView.playAnimation();
    }
}
