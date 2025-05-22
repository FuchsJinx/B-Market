package com.karpeko.coffee.ui.account;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.R;
import com.karpeko.coffee.account.LoginActivity;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    private TextView textViewName, textViewEmail;
    private Button buttonLogout, forgotPassword;
    private ImageButton editAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        textViewName = view.findViewById(R.id.textViewName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        editAccountButton = view.findViewById(R.id.editAccountButton);
        forgotPassword = view.findViewById(R.id.forgotPassword);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        showUserInformation(currentUser);

        editAccountButton.setOnClickListener(v -> {
            openEditProfileDialog();
        });
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ForgotPasswordActivity.class));
        });

        return view;
    }

    private void goToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showUserInformation(FirebaseUser currentUser) {
        if (currentUser != null) {
            // Отобразим email из FirebaseUser
            textViewEmail.setText(currentUser.getEmail());

            // Получим userId из SharedPreferences, если нужно
            SharedPreferences prefs = requireActivity().getSharedPreferences("accountPrefs", MODE_PRIVATE);
            String userId = prefs.getString("userId", currentUser.getUid());

            // Получаем дополнительные данные пользователя из Firestore
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("username"); // или "name" в зависимости от структуры

                            if (name != null && !name.isEmpty()) {
                                textViewName.setText(name);
                            } else {
                                textViewName.setText("Имя не указано");
                            }
                        } else {
                            textViewName.setText("Пользователь не найден");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Если пользователь не авторизован, отправляем на экран входа
            goToLogin();
        }

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();

            // Очистка SharedPreferences
            SharedPreferences prefs = requireActivity().getSharedPreferences("accountPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            goToLogin();
        });
    }

    private void openEditProfileDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = user.getDisplayName();
                    if (documentSnapshot.exists()) {
                        String firestoreUsername = documentSnapshot.getString("username");
                        if (firestoreUsername != null && !firestoreUsername.isEmpty()) {
                            username = firestoreUsername;
                        }
                    }

                    String email = user.getEmail();

                    EditProfileDialog dialog = new EditProfileDialog(getContext(), username, email, requireActivity());
                    dialog.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show();
                });
    }

}