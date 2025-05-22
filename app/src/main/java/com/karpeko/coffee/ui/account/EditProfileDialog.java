package com.karpeko.coffee.ui.account;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.Tasks;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.R;

import java.util.HashMap;
import java.util.Map;

public class EditProfileDialog extends Dialog {

    private EditText editTextUsername, editTextEmail, editTextNewPassword, editTextCurrentPassword;
    private Button buttonSave;

    private String currentUsername, currentEmail;
    private Context context;
    private Task<Void> emailTask;
    private Task<Void> passwordTask;
    private Activity activity;

    public EditProfileDialog(@NonNull Context context, String username, String email, Activity activity) {
        super(context);
        this.context = context;
        this.currentUsername = username;
        this.currentEmail = email;
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_profile);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        buttonSave = findViewById(R.id.buttonSave);

        // Заполняем текущими данными
        editTextUsername.setText(currentUsername);
        editTextEmail.setText(currentEmail);

        buttonSave.setOnClickListener(v -> saveChanges(activity));
    }

    private void saveChanges(Activity activity) {
        String newUsername = editTextUsername.getText().toString().trim();
        String newEmail = editTextEmail.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString();
        String currentPassword = editTextCurrentPassword.getText().toString();

        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(context, "Введите текущий пароль для подтверждения", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newUsername)) {
            Toast.makeText(context, "Имя пользователя не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newEmail)) {
            Toast.makeText(context, "Email не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                // Обновляем email, если изменился
                emailTask = Tasks.forResult(null);
                if (!newEmail.equals(user.getEmail())) {
                    emailTask = user.updateEmail(newEmail);
                }

                emailTask.addOnCompleteListener(emailUpdateTask -> {
                    if (emailUpdateTask.isSuccessful()) {
                        // Обновляем пароль, если задан
                        passwordTask = Tasks.forResult(null);
                        if (!TextUtils.isEmpty(newPassword)) {
                            passwordTask = user.updatePassword(newPassword);
                        }

                        passwordTask.addOnCompleteListener(passwordUpdateTask -> {
                            if (passwordUpdateTask.isSuccessful()) {
                                // Обновляем username в Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("username", newUsername);

                                db.collection("users").document(user.getUid())
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Профиль успешно обновлён", Toast.LENGTH_SHORT).show();
                                            activity.recreate();
                                            dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Ошибка обновления имени пользователя", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(context, "Ошибка обновления пароля", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(context, "Ошибка обновления email", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Неверный текущий пароль", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
