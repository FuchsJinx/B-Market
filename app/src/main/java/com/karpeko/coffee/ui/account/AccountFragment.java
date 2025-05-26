package com.karpeko.coffee.ui.account;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.R;
import com.karpeko.coffee.account.LoginActivity;
import com.karpeko.coffee.account.UserSessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AccountFragment extends Fragment {

    private TextView textViewName, textViewEmail, forgotPassword;
    private Button buttonLogout;
    private ImageButton editAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserSessionManager sessionManager;
    ImageView icon;
    private static final int PICK_IMAGE_REQUEST = 1;
    FirebaseUser currentUser;

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

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null || !sessionManager.isLoggedIn()) {
            goToLogin();
            return view;
        }

        showUserInformation(currentUser);

        editAccountButton.setOnClickListener(v -> openEditProfileDialog());
        forgotPassword.setOnClickListener(v -> openForgotPasswordDialog());

        buttonLogout.setOnClickListener(v -> logout());

        icon = view.findViewById(R.id.icon);
        icon.setOnClickListener(v -> {
            openImageChooser();
        });

        loadAvatarForUser();

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

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private void saveAvatarPathForUser(String path) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserAvatars", Context.MODE_PRIVATE);
        String userEmail = currentUser.getEmail();
        if (userEmail == null) return;

        prefs.edit().putString(userEmail + "_avatar_path", path).apply();
    }

    private void loadAvatarForUser() {
        String userEmail = currentUser.getEmail();
        if (userEmail == null) {
            icon.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserAvatars", Context.MODE_PRIVATE);
        String path = prefs.getString(userEmail + "_avatar_path", null);

        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                icon.setImageURI(Uri.fromFile(file));
                return;
            } else {
                // Файл не найден, удаляем устаревший путь
                prefs.edit().remove(userEmail + "_avatar_path").apply();
            }
        }
        icon.setImageResource(R.drawable.ic_launcher_foreground);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                String userEmail = currentUser.getEmail();
                if (userEmail != null) {
                    String savedPath = copyImageToInternalStorage(requireContext(), imageUri, userEmail + "_avatar.jpg");
                    icon.setImageURI(Uri.fromFile(new File(savedPath)));
                    saveAvatarPathForUser(savedPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Ошибка при сохранении изображения", Toast.LENGTH_SHORT).show();
            }
        }
        requireActivity().recreate();
    }

    private String copyImageToInternalStorage(Context context, Uri uri, String fileName) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) throw new IOException("Невозможно открыть InputStream");

        File file = new File(context.getFilesDir(), fileName);
        OutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return file.getAbsolutePath();
    }
}
