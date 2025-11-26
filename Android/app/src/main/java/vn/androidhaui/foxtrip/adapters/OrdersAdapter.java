package vn.androidhaui.foxtrip.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.databinding.ItemOrderBinding;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.models.OrderItem;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
    private final List<Order> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onClick(Order order); }

    public OrdersAdapter(OnItemClickListener listener) { this.listener = listener; }

    public void setItems(List<Order> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter.ViewHolder holder, int position) {
        Order o = items.get(position);
        holder.bind(o);
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding b;
        ViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Order o) {
            b.tvOrderId.setText(o.getId() != null ? "Mã đơn: " + o.getId() : "—");
            b.tvStatus.setText(o.getStatus() != null ? "Trạng thái: " + o.getStatus() : "—");
            b.tvTotal.setText("Tổng: " + String.format("%,.0f đ", o.getTotalAmount()));

            // show thumbnail from first item nếu có
            String thumb = null;
            if (o.getItems() != null && !o.getItems().isEmpty()) {
                OrderItem first = o.getItems().get(0);
                thumb = first.getImage();
            }
            if (thumb != null && !thumb.isEmpty()) {
                Glide.with(b.getRoot().getContext()).load(thumb).into(b.ivThumb);
            } else {
                b.ivThumb.setImageResource(android.R.color.transparent);
            }

            b.getRoot().setOnClickListener(v -> listener.onClick(o));
        }
    }
}
