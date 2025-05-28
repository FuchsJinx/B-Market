package com.karpeko.coffee.ui.orders.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.orders.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<Order> orders;
    private HistoryAdapter.OnHistoryClickListener listener;

    public HistoryAdapter(List<Order> orders, HistoryAdapter.OnHistoryClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onHistoryClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvOrderId, tvStatus, tvDate, tvTotal;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }

        void bind(Order order) {
            tvOrderId.setText("Заказ №" + order.getOrderId());

            tvStatus.setText(order.getStatus());

            Timestamp createdAt = order.getCreatedAt();
            if (createdAt != null) {
                Date date = createdAt.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                tvDate.setText(sdf.format(date));
            } else {
                tvDate.setText("");
            }

            tvTotal.setText(String.format(Locale.getDefault(), "%.2f ₽", order.getTotal()));
        }
    }

    public interface OnHistoryClickListener {
        void onHistoryClick(Order order);
    }
}



