package com.karpeko.coffee.ui.menu.lists.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.karpeko.coffee.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.karpeko.coffee.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionViewHolder> {

    private final List<String> groupNames;
    private final Map<String, List<String>> optionsMap;
    private final Map<String, String> selectedOptions = new HashMap<>();

    public OptionsAdapter(Map<String, List<String>> optionsMap) {
        this.optionsMap = optionsMap;
        this.groupNames = new ArrayList<>(optionsMap.keySet());

        for (String group : groupNames) {
            List<String> values = optionsMap.get(group);
            if (values != null && !values.isEmpty()) {
                selectedOptions.put(group, values.get(0));
            }
        }
    }

    public void setSelectedOptions(Map<String, String> selectedOptionsFromIntent) {
        for (Map.Entry<String, String> entry : selectedOptionsFromIntent.entrySet()) {
            if (optionsMap.containsKey(entry.getKey())) {
                List<String> values = optionsMap.get(entry.getKey());
                if (values.contains(entry.getValue())) {
                    selectedOptions.put(entry.getKey(), entry.getValue());
                }
            }
        }
        notifyDataSetChanged();
    }

    public Map<String, String> getSelectedOptions() {
        return selectedOptions;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.option_item, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        String groupName = groupNames.get(position);
        List<String> values = optionsMap.get(groupName);
        holder.bind(groupName, values);
    }

    @Override
    public int getItemCount() {
        return groupNames.size();
    }

    public class OptionViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;
        Spinner spinner;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.optionGroupName);
            spinner = itemView.findViewById(R.id.optionSpinner);
        }

        public void bind(String groupName, List<String> values) {
            groupNameTextView.setText(groupName);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_item, values);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            // Устанавливаем выбранный вариант, если есть
            String selectedValue = selectedOptions.get(groupName);
            if (selectedValue != null) {
                int index = values.indexOf(selectedValue);
                if (index >= 0) spinner.setSelection(index);
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedOptions.put(groupName, values.get(position));
                    view.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(150)
                            .withEndAction(() -> view.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                                    .start())
                            .start();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }
}
