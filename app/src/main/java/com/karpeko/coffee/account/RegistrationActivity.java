package com.karpeko.coffee.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.karpeko.coffee.R;

public class RegistrationActivity extends BaseAuthActivity {

    EditText usernameText, emailText, passwordText;
    Button registrationButton, registrationWithGoogle;
    TextView toLogin, loginOut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usernameText = findViewById(R.id.usernameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        registrationButton = findViewById(R.id.registrationButton);
        registrationWithGoogle = findViewById(R.id.registrationWithGoogle);
        toLogin = findViewById(R.id.toLogin);
        loginOut = findViewById(R.id.loginOut);

        registrationButton.setOnClickListener(v -> registerUser());
        registrationWithGoogle.setOnClickListener(v -> signInWithGoogle());
        toLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        loginOut.setOnClickListener(v -> startMainActivity());
    }

    private void registerUser() {
        String username = usernameText.getText().toString().trim();
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameText.setError("Введите имя пользователя");
            usernameText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailText.setError("Введите email");
            emailText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordText.setError("Пароль должен быть не менее 6 символов");
            passwordText.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userID = firebaseUser.getUid();
                            User user = new User(username, email);
                            db.collection("users").document(userID).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegistrationActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                                        saveUserPreferences(userID);
                                        startMainActivity();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RegistrationActivity.this, "Ошибка при сохранении данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
