package com.karpeko.coffee.ui.account;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.karpeko.coffee.R;

import com.airbnb.lottie.LottieAnimationView;

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textView = findViewById(R.id.click);
        TextView textView1 = findViewById(R.id.pol);
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation(R.raw.infinity_animation);

        // Запускаем анимацию (если не включено в XML)
        lottieAnimationView.playAnimation();

        textView.setOnClickListener(v -> {
            textView1.setVisibility(VISIBLE);
            textView.setVisibility(GONE);
        });

        textView1.setOnClickListener(v -> {
            textView1.setVisibility(GONE);
            textView.setVisibility(VISIBLE);
        });
    }
}
