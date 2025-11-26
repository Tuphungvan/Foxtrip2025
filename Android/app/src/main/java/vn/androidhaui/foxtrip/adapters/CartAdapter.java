package vn.androidhaui.foxtrip.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.androidhaui.foxtrip.databinding.ItemCartBinding;
import vn.androidhaui.foxtrip.models.CartItem;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.VH> {

    // 🟡 Thêm 2 mode
    public static final int MODE_EDITABLE = 0;
    public static final int MODE_READONLY = 1;

    private int mode = MODE_EDITABLE;  // mặc định là giỏ hàng

    private final Context ctx;
    private CartActionListener listener;

    public interface CartActionListener {
        void onIncrease(@NonNull CartItem item);
        void onDecrease(@NonNull CartItem item);
        void onRemove(@NonNull CartItem item);
        void onItemClick(@NonNull CartItem item);
    }

    public void setCartActionListener(CartActionListener l) {
        this.listener = l;
    }

    // 🟡 Hàm này để fragment đổi chế độ
    public void setMode(int mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    public CartAdapter(Context ctx) {
        super(DIFF_CALLBACK);
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding b = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItem it = getItem(position);
        holder.binding.tvName.setText(it.getName());
        holder.binding.tvFinalPrice.setText(String.format("%,.0f VND",
                it.getFinalPrice() != null ? it.getFinalPrice() : 0.0));

        // 🟡 Chế độ hiển thị số lượng khác nhau
        if (mode == MODE_READONLY) {
            holder.binding.tvQuantity.setText("Số lượng: " + it.getQuantity());
        } else {
            holder.binding.tvQuantity.setText(String.valueOf(it.getQuantity()));
        }

        String img = it.getImage();
        if (img != null && !img.isEmpty()) {
            Glide.with(ctx).load(img).into(holder.binding.ivImage);
        } else {
            holder.binding.ivImage.setImageResource(android.R.color.transparent);
        }

        // 🟡 Ẩn/Hiện nút theo mode
        if (mode == MODE_READONLY) {
            holder.binding.btnIncrease.setVisibility(View.GONE);
            holder.binding.btnDecrease.setVisibility(View.GONE);
            holder.binding.btnRemove.setVisibility(View.GONE);
        } else {
            holder.binding.btnIncrease.setVisibility(View.VISIBLE);
            holder.binding.btnDecrease.setVisibility(View.VISIBLE);
            holder.binding.btnRemove.setVisibility(View.VISIBLE);
        }

        // Các listener chỉ hoạt động ở chế độ editable
        holder.binding.btnIncrease.setOnClickListener(v -> {
            if (listener != null && mode == MODE_EDITABLE) listener.onIncrease(it);
        });

        holder.binding.btnDecrease.setOnClickListener(v -> {
            if (listener != null && mode == MODE_EDITABLE) listener.onDecrease(it);
        });

        holder.binding.btnRemove.setOnClickListener(v -> {
            if (listener != null && mode == MODE_EDITABLE) listener.onRemove(it);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(it);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;
        VH(@NonNull ItemCartBinding b) { super(b.getRoot()); binding = b; }
    }

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CartItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
                    return oldItem.getSlug().equals(newItem.getSlug());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
                    return oldItem.equals(newItem) &&
                            oldItem.getQuantity().equals(newItem.getQuantity()) &&
                            Double.valueOf(oldItem.getFinalPrice())
                                    .equals(Double.valueOf(newItem.getFinalPrice()));
                }
            };
}
