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

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionViewHolder> {
    private Map<String, List<String>> options;
    private Map<String, String> selectedOptions = new HashMap<>();

    public void setOptions(Map<String, List<String>> options) {
        this.options = options;
        notifyDataSetChanged();
    }

    public Map<String, String> getSelectedOptions() {
        return selectedOptions;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        String optionName = new ArrayList<>(options.keySet()).get(position);

        holder.bind(optionName, options.get(optionName));
    }

    @Override
    public int getItemCount() {
        return options != null ? options.size() : 0;
    }

    class OptionViewHolder extends RecyclerView.ViewHolder {
        private TextView optionTitle;
        private RadioGroup variantsGroup;

        public OptionViewHolder(View itemView) {
            super(itemView);
            optionTitle = itemView.findViewById(R.id.option_name);
            variantsGroup = itemView.findViewById(R.id.variants_group);
        }

        public void bind(String optionName, List<String> variants) {
            optionTitle.setText(optionName);
            variantsGroup.removeAllViews();
            variantsGroup.setOnCheckedChangeListener(null);

            for (String variant : variants) {
                RadioButton radioButton = new RadioButton(itemView.getContext());
                radioButton.setText(variant);
                variantsGroup.addView(radioButton);
            }

            variantsGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selected = itemView.findViewById(checkedId);
                if (selected != null) {
                    // Записываем выбранный вариант в список (с одним элементом)
                    List<String> selectedList = new ArrayList<>();
                    selectedList.add(selected.getText().toString());
                    selectedOptions.put(optionName, String.valueOf(selectedList));
                }
            });
        }
    }
}