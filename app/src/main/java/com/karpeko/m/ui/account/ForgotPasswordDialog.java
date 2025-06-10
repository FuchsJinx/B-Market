package com.karpeko.m.ui.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.karpeko.m.R;

public class ForgotPasswordDialog extends Dialog {

    private EditText editTextEmail;
    private Button buttonResetPassword;
    private FirebaseAuth mAuth;
    private Context context;

    public ForgotPasswordDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_forgot_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        buttonResetPassword.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Введите email");
            editTextEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context,
                                "Письмо для восстановления пароля отправлено на почту",
                                Toast.LENGTH_LONG).show();
                        dismiss();
                    } else {
                        Toast.makeText(context,
                                "Ошибка: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
