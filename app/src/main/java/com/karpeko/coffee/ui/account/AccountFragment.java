package com.karpeko.coffee.ui.account;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.R;
import com.karpeko.coffee.account.LoginActivity;
import com.karpeko.coffee.account.UserSessionManager;

public class AccountFragment extends Fragment {

    private TextView textViewName, textViewEmail;
    private Button buttonLogout, forgotPassword;
    private ImageButton editAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserSessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
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
        sessionManager = new UserSessionManager(requireContext());

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null || !sessionManager.isLoggedIn()) {
            goToLogin();
            return view;
        }

        showUserInformation(currentUser);

        editAccountButton.setOnClickListener(v -> openEditProfileDialog());
        forgotPassword.setOnClickListener(v -> openForgotPasswordDialog());

        buttonLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void showUserInformation(FirebaseUser currentUser) {
        textViewEmail.setText(currentUser.getEmail());

        String userId = sessionManager.getUserId();
        if (userId == null) userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        textViewName.setText(!TextUtils.isEmpty(name) ? name : "Имя не указано");
                    } else {
                        textViewName.setText("Пользователь не найден");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show()
                );
    }

    private void logout() {
        mAuth.signOut();
        sessionManager.clearSession();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void openEditProfileDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = user.getDisplayName();
                    if (documentSnapshot.exists()) {
                        String firestoreUsername = documentSnapshot.getString("username");
                        if (!TextUtils.isEmpty(firestoreUsername)) {
                            username = firestoreUsername;
                        }
                    }

                    String email = user.getEmail();

                    EditProfileDialog dialog = new EditProfileDialog(getContext(), username, email, requireActivity());
                    dialog.show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show()
                );
    }

    private void openForgotPasswordDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        ForgotPasswordDialog dialog = new ForgotPasswordDialog(requireContext());
        dialog.show();
    }
}
