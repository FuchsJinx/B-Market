package com.karpeko.coffee.ui.orders.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.karpeko.coffee.R;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItemDisplay> items;

    public OrderItemAdapter(List<OrderItemDisplay> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_row, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textQuantity, textOptions;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textPrice = itemView.findViewById(R.id.textOrderItemPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textOptions = itemView.findViewById(R.id.textOptions);
        }

        public void bind(OrderItemDisplay item) {
            textName.setText(item.getName());
            textPrice.setText(item.getPrice() + " ₽");
            textQuantity.setText("×" + item.getQuantity());
            textOptions.setText(item.getOptions());
        }
    }
}

