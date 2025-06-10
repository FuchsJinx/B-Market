package com.karpeko.m.ui.account;

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
import com.karpeko.m.R;
import com.karpeko.m.account.UserSessionManager;

import java.util.HashMap;
import java.util.Map;

public class EditProfileDialog extends Dialog {

    private EditText editTextUsername, editTextEmail, editTextNewPassword, editTextCurrentPassword;
    private Button buttonSave;
    private String currentUsername, currentEmail;
    private Context context;
    private Activity activity;
    private UserSessionManager sessionManager;

    public EditProfileDialog(@NonNull Context context, String username, String email, Activity activity) {
        super(context);
        this.context = context;
        this.currentUsername = username;
        this.currentEmail = email;
        this.activity = activity;
        this.sessionManager = new UserSessionManager(context);
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

        editTextUsername.setText(currentUsername);
        editTextEmail.setText(currentEmail);

        buttonSave.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String newUsername = editTextUsername.getText().toString().trim();
        String newEmail = editTextEmail.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString();
        String currentPassword = editTextCurrentPassword.getText().toString();

        if (TextUtils.isEmpty(currentPassword)) {
            showToast("Введите текущий пароль для подтверждения");
            return;
        }
        if (TextUtils.isEmpty(newUsername)) {
            showToast("Имя пользователя не может быть пустым");
            return;
        }
        if (TextUtils.isEmpty(newEmail)) {
            showToast("Email не может быть пустым");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToast("Пользователь не авторизован");
            dismiss();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(authTask -> {
                    if (!authTask.isSuccessful()) {
                        showToast("Неверный текущий пароль");
                        return;
                    }
                    updateEmailIfNeeded(user, newEmail)
                            .continueWithTask(emailUpdateTask -> {
                                if (!emailUpdateTask.isSuccessful()) {
                                    throw emailUpdateTask.getException();
                                }
                                return updatePasswordIfNeeded(user, newPassword);
                            })
                            .continueWithTask(passwordUpdateTask -> {
                                if (!passwordUpdateTask.isSuccessful()) {
                                    throw passwordUpdateTask.getException();
                                }
                                return updateUsernameInFirestore(user.getUid(), newUsername);
                            })
                            .addOnSuccessListener(aVoid -> {
                                showToast("Профиль успешно обновлён");
                                // Обновляем сессию, если email изменился
                                if (!newEmail.equalsIgnoreCase(currentEmail)) {
                                    sessionManager.setUserLoggedIn(user.getUid());
                                }
                                activity.recreate();
                                dismiss();
                            })
                            .addOnFailureListener(e -> {
                                showToast("Ошибка обновления профиля: " + e.getMessage());
                            });
                });
    }

    private Task<Void> updateEmailIfNeeded(FirebaseUser user, String newEmail) {
        if (!newEmail.equalsIgnoreCase(user.getEmail())) {
            return user.updateEmail(newEmail);
        } else {
            return Tasks.forResult(null);
        }
    }

    private Task<Void> updatePasswordIfNeeded(FirebaseUser user, String newPassword) {
        if (!TextUtils.isEmpty(newPassword)) {
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("Пароль должен содержать не менее 6 символов");
            }
            return user.updatePassword(newPassword);
        } else {
            return Tasks.forResult(null);
        }
    }

    private Task<Void> updateUsernameInFirestore(String userId, String newUsername) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        return db.collection("users").document(userId).update(updates);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
