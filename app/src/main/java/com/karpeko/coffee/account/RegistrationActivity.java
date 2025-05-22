package com.karpeko.coffee.account;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.karpeko.coffee.MainActivity;
import com.karpeko.coffee.R;

public class RegistrationActivity extends AppCompatActivity {

    EditText usernameText, emailText, passwordText;
    Button registrationButton, registrationWithGoogle;
    TextView toLogin, loginOut;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final int RC_SIGN_IN = 9001;
    GoogleSignInClient googleSignInClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ваш OAuth 2.0 client ID из google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        usernameText = findViewById(R.id.usernameText);
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        registrationButton = findViewById(R.id.registrationButton);
        registrationWithGoogle = findViewById(R.id.registrationWithGoogle);
        toLogin = findViewById(R.id.toLogin);
        loginOut = findViewById(R.id.loginOut);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registrationButton.setOnClickListener(v -> registerUser());
        registrationWithGoogle.setOnClickListener(v -> signInWithGoogle());
        toLogin.setOnClickListener(v -> startActivity(new Intent(this, com.karpeko.coffee.account.LoginActivity.class)));
        loginOut.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
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

        // Регистрируем пользователя в Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userID = firebaseUser.getUid();
                            // Создаём объект User с текущим временем
                            User user = new User(username, email);

                            // Используем userId как имя документа
                            db.collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegistrationActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                                        saveUserPreferences(userID);
                                        startActivity(new Intent(this, MainActivity.class));
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegistrationActivity.this, "Ошибка при сохранении данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userID = firebaseUser.getUid();
                            String email = firebaseUser.getEmail();
                            String username = firebaseUser.getDisplayName();

                            User user = new User(username, email);

                            // Проверяем, есть ли пользователь в БД
                            db.collection("users").document(userID).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (!documentSnapshot.exists()) {
                                            // Если нет - создаём
                                            db.collection("users").document(userID).set(user);
                                        }
                                        // Далее переход в главное окно
                                        saveUserPreferences(userID);
                                        startActivity(new Intent(this, MainActivity.class));
                                    });
                        }
                    } else {
                        // Ошибка аутентификации
                        Toast.makeText(this, "Ошибка аутентицикации", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String idToken = account.getIdToken();  // Вот он - нужный токен
                    firebaseAuthWithGoogle(idToken);        // Передаём токен в Firebase
                }
            } catch (ApiException e) {
                // Обработка ошибки входа через Google
                e.printStackTrace();
            }
        }
    }

    private void saveUserPreferences(String userId) {
        SharedPreferences preferences = getSharedPreferences("accountPrefs", MODE_PRIVATE);
        preferences.edit().putBoolean("isLoggedIn", true).putString("userId", userId).apply();
    }
}