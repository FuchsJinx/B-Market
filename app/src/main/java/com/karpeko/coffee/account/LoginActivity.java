package com.karpeko.coffee.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.karpeko.coffee.R;

public class LoginActivity extends BaseAuthActivity {

    EditText emailText, passwordText;
    Button loginButton, loginWithGoogle;
    TextView toRegistration, loginOut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        loginWithGoogle = findViewById(R.id.loginWithGoogle);
        toRegistration = findViewById(R.id.toRegistration);
        loginOut = findViewById(R.id.loginOut);

        loginButton.setOnClickListener(v -> loginUser());
        loginWithGoogle.setOnClickListener(v -> signInWithGoogle());
        toRegistration.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        });
        loginOut.setOnClickListener(v -> startMainActivity());
    }

    private void loginUser() {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailText.setError("Введите email");
            emailText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordText.setError("Введите пароль");
            passwordText.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
