package com.karpeko.coffee.ui.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.menu.lists.base.Base;
import com.karpeko.coffee.ui.menu.lists.base.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(com.karpeko.coffee.R.layout.fragment_menu, container, false);

        List<Base> categotyList = new ArrayList<>();
        RecyclerView categories = view.findViewById(R.id.categories);

        categotyList.add(new Base("Кофе"));
        categotyList.add(new Base("Чай"));
        categotyList.add(new Base("Завтраки"));
        categotyList.add(new Base("Выпечка"));

        BaseAdapter adapter = new BaseAdapter(getContext(), categotyList);
        categories.setAdapter(adapter);
        categories.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}