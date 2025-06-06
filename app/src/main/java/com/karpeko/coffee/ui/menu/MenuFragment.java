package com.karpeko.coffee.ui.menu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.menu.lists.base.Base;
import com.karpeko.coffee.ui.menu.lists.base.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import com.airbnb.lottie.LottieAnimationView;

public class MenuFragment extends Fragment {

    private RecyclerView categories;
    private BaseAdapter adapter;
    private List<Base> categoryList = new ArrayList<>();
    private LottieAnimationView lottieLoading; // заменили ProgressBar на LottieAnimationView

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        categories = view.findViewById(R.id.categories);
        lottieLoading = view.findViewById(R.id.lottieLoading);
        lottieLoading.setAnimation(R.raw.progress_animation);

        adapter = new BaseAdapter(getContext(), categoryList);
        categories.setAdapter(adapter);
        categories.setLayoutManager(new LinearLayoutManager(getContext()));

        loadCategoriesFromFirestore();

        return view;
    }

    private void loadCategoriesFromFirestore() {
        showLoading(true);

        FirebaseFirestore.getInstance()
                .collection("menu_categories")
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful() && task.getResult() != null) {
                        categoryList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String imageUrl = document.getString("image");
                            if (name != null) {
                                categoryList.add(new Base(name, imageUrl));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        showError("Ошибка загрузки категорий");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError(e.getMessage());
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();
            categories.setVisibility(View.GONE);
        } else {
            lottieLoading.cancelAnimation();
            lottieLoading.setVisibility(View.GONE);
            categories.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
