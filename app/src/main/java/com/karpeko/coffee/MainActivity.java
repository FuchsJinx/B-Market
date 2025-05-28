package com.karpeko.coffee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.account.UserSessionManager;
import com.karpeko.coffee.databinding.ActivityMainBinding;
import com.karpeko.coffee.ui.orders.OrderActivity;
import com.karpeko.coffee.ui.orders.history.OrderWorkScheduler;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private UserSessionManager userSessionManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("WORKER", "Попытка запуска Worker");
        OrderWorkScheduler.scheduleOrderCheck(this);

        userSessionManager = new UserSessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavigationView navigationView = binding.navView;

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderActivity.class));
        });
        DrawerLayout drawer = binding.drawerLayout;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_menu, R.id.nav_orders, R.id.nav_account)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_menu) {
                navController.navigate(R.id.nav_menu);
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, OrderActivity.class));
            } else if (itemId == R.id.nav_account) {
                navController.navigate(R.id.nav_account);
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        if (userSessionManager.isLoggedIn()) {
            View header = navigationView.getHeaderView(0);
            setUserInformation(header, currentUser);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setUserInformation(View header, FirebaseUser currentUser) {
        TextView textViewName = header.findViewById(R.id.textViewName);
        TextView textViewEmail = header.findViewById(R.id.textViewEmail);
        ImageView imageViewAvatar = header.findViewById(R.id.imageViewAvatar);

        String userId = userSessionManager.getUserId();
        if (userId == null) userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        textViewName.setText(!TextUtils.isEmpty(name) ? name : "Имя не указано");
                        textViewEmail.setText(currentUser.getEmail());
                        loadAvatarForUser(imageViewAvatar);
                    } else {
                        textViewName.setText("Пользователь не найден");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadAvatarForUser(ImageView icon) {
        String userEmail = currentUser.getEmail();
        if (userEmail == null) {
            icon.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserAvatars", Context.MODE_PRIVATE);
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
}