package vn.androidhaui.foxtrip.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import vn.androidhaui.foxtrip.databinding.ItemOrderAdminBinding;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.models.OrderItem;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.VH> {

    public interface OnActionListener {
        void onDeletePending(Order order);
        void onConfirmPaid(Order order);
        void onConfirmExpired(Order order);
        void onItemClicked(Order order);
    }

    private final Context ctx;
    private final List<Order> list;
    private final String type; // "pending", "to_confirm", "completed"
    private final OnActionListener listener;

    public AdminOrdersAdapter(Context ctx, List<Order> list, String type, OnActionListener listener) {
        this.ctx = ctx;
        this.list = list;
        this.type = type;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemOrderAdminBinding binding = ItemOrderAdminBinding.inflate(inflater, parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Order o = list.get(position);
        OrderItem first = (o.getItems() != null && !o.getItems().isEmpty()) ? o.getItems().get(0) : null;

        holder.binding.tvTitle.setText(first != null ? first.getName() : "Đơn hàng");
        holder.binding.tvQuantity.setText("Số lượng: " + (first != null ? first.getQuantity() : 0));
        holder.binding.tvPrice.setText("Tổng: " + formatCurrency(o.getTotalAmount()));
        holder.binding.tvStatus.setText(o.getStatus());
        holder.binding.tvCreated.setText(formatDate(o.getCreatedAt()));

        if (first != null && first.getImage() != null && !first.getImage().isEmpty()) {
            Glide.with(ctx).load(first.getImage()).centerCrop().into(holder.binding.ivThumb);
        } else {
            holder.binding.ivThumb.setImageResource(vn.androidhaui.foxtrip.R.drawable.ic_image_placeholder);
        }

        // Configure action button by type
        switch (type) {
            case "pending":
                holder.binding.btnAction.setText("Xóa");
                holder.binding.btnAction.setOnClickListener(v -> {
                    if (listener != null) listener.onDeletePending(o);
                });
                break;
            case "to_confirm":
                holder.binding.btnAction.setText("Xác nhận");
                holder.binding.btnAction.setOnClickListener(v -> {
                    if (listener != null) listener.onConfirmPaid(o);
                });
                break;
            case "completed":
                holder.binding.btnAction.setText("Hoàn tất đơn");
                holder.binding.btnAction.setOnClickListener(v -> {
                    if (listener != null) listener.onConfirmExpired(o);
                });
                break;
            default:
                holder.binding.btnAction.setVisibility(View.GONE);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClicked(o);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class VH extends RecyclerView.ViewHolder {
        final ItemOrderAdminBinding binding;
        public VH(@NonNull ItemOrderAdminBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    private String formatCurrency(Double value) {
        if (value == null) value = 0.0;
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi","VN"));
        return nf.format(value) + "đ";
    }

    private String formatDate(String iso) {
        if (iso == null) return "";
        // try to make it human — simple parse for ISO
        try {
            // naive split to date part
            String datePart = iso.split("T")[0];
            return datePart;
        } catch (Exception e) {
            return iso;
        }
    }
}
