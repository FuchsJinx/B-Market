package com.karpeko.coffee.account;

import android.content.Intent;
import android.os.Bundle;
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

public abstract class BaseAuthActivity extends AppCompatActivity {

    protected FirebaseAuth mAuth;
    protected FirebaseFirestore db;
    protected GoogleSignInClient googleSignInClient;
    protected static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    protected void signInWithGoogle() {
        // Отзываем доступ, чтобы всегда показывать выбор аккаунта
        googleSignInClient.revokeAccess()
                .addOnCompleteListener(task -> {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                });
    }

    protected void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            handleUserAfterGoogleSignIn(firebaseUser);
                        }
                    } else {
                        Toast.makeText(this, "Ошибка аутентификации", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void handleUserAfterGoogleSignIn(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String username = firebaseUser.getDisplayName();

        User user = new User(username, email);

        db.collection("users").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        db.collection("users").document(userID).set(user);
                    }
                    UserSessionManager userSessionManager = new UserSessionManager(this);
                    userSessionManager.setUserLoggedIn(userID);
                    startMainActivity();
                });
    }

    protected void saveUserPreferences(String userId) {
        getSharedPreferences("accountPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", true)
                .putString("userId", userId)
                .apply();
    }

    protected void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }
}

