package com.karpeko.m.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.m.MainActivity;
import com.karpeko.m.R;

public abstract class BaseAuthActivity extends AppCompatActivity {

    protected FirebaseAuth mAuth;
    protected FirebaseFirestore db;
    protected GoogleSignInClient googleSignInClient;
    protected static final int RC_SIGN_IN = 9001;
    private static final String TAG = "BaseAuthActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: Инициализация GoogleSignInOptions");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        Log.d(TAG, "onCreate: Получение GoogleSignInClient");
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Log.d(TAG, "onCreate: Получение экземпляра FirebaseAuth");
        mAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreate: Получение экземпляра Firestore");
        db = FirebaseFirestore.getInstance();
    }

    protected void signInWithGoogle() {
        Log.d(TAG, "signInWithGoogle: Отзыв доступа для показа выбора аккаунта");
        googleSignInClient.revokeAccess()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "signInWithGoogle: revokeAccess завершён, запуск signInIntent");
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                });
    }

    protected void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogle: Начало аутентификации с Firebase, idToken=" + (idToken != null ? "получен" : "null"));
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "firebaseAuthWithGoogle: Аутентификация успешна");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            handleUserAfterGoogleSignIn(firebaseUser);
                        } else {
                            Log.e(TAG, "firebaseAuthWithGoogle: FirebaseUser == null после успешной аутентификации");
                        }
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "firebaseAuthWithGoogle: Ошибка аутентификации", exception);
                        Toast.makeText(this, "Ошибка аутентификации: " + (exception != null ? exception.getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
    }

    protected void handleUserAfterGoogleSignIn(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String username = firebaseUser.getDisplayName();

        Log.d(TAG, "handleUserAfterGoogleSignIn: userID=" + userID + ", email=" + email + ", username=" + username);

        User user = new User(username, email);

        db.collection("users").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "handleUserAfterGoogleSignIn: Проверка существования пользователя в Firestore");
                    if (!documentSnapshot.exists()) {
                        Log.d(TAG, "handleUserAfterGoogleSignIn: Новый пользователь, добавление в Firestore");
                        db.collection("users").document(userID).set(user)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "handleUserAfterGoogleSignIn: Пользователь успешно добавлен в Firestore"))
                                .addOnFailureListener(e -> Log.e(TAG, "handleUserAfterGoogleSignIn: Ошибка добавления пользователя в Firestore", e));
                    } else {
                        Log.d(TAG, "handleUserAfterGoogleSignIn: Пользователь уже существует в Firestore");
                    }
                    UserSessionManager userSessionManager = new UserSessionManager(this);
                    userSessionManager.setUserLoggedIn(userID);
                    Log.d(TAG, "handleUserAfterGoogleSignIn: Сессия пользователя сохранена, запуск MainActivity");
                    startMainActivity();
                })
                .addOnFailureListener(e -> Log.e(TAG, "handleUserAfterGoogleSignIn: Ошибка получения пользователя из Firestore", e));
    }

    protected void saveUserPreferences(String userId) {
        Log.d(TAG, "saveUserPreferences: Сохранение userId=" + userId + " в SharedPreferences");
        getSharedPreferences("accountPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", true)
                .putString("userId", userId)
                .apply();
    }

    protected void startMainActivity() {
        Log.d(TAG, "startMainActivity: Запуск MainActivity");
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: Обработка результата Google Sign-In");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d(TAG, "onActivityResult: GoogleSignInAccount получен, idToken=" + (account.getIdToken() != null ? "есть" : "нет"));
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Log.e(TAG, "onActivityResult: GoogleSignInAccount == null");
                }
            } catch (ApiException e) {
                Log.e(TAG, "onActivityResult: Ошибка Google Sign-In", e);
                Toast.makeText(this, "Ошибка Google Sign-In: " + e.getStatusCode() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
