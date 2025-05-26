package com.karpeko.coffee.ui.menu.lists.base;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.menu.lists.category.CategoryActivity;

import java.util.List;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class BaseAdapter extends RecyclerView.Adapter<BaseAdapter.BaseViewHolder> {

    private Context context;
    private List<Base> categoryList;

    public BaseAdapter(Context context, List<Base> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_base, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        Base category = categoryList.get(position);
        holder.text.setText(category.getCategory());

        String imageUrl = category.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.categoryImage);
        } else {
            holder.categoryImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView categoryImage;

        public BaseViewHolder(@NonNull View baseView) {
            super(baseView);
            text = baseView.findViewById(R.id.text);
            categoryImage = baseView.findViewById(R.id.categoryImage);

            text.setOnClickListener(v -> {
                Intent intent = new Intent(context, CategoryActivity.class);
                intent.putExtra("category", text.getText().toString());
                context.startActivity(intent);
            });
        }
    }
}
