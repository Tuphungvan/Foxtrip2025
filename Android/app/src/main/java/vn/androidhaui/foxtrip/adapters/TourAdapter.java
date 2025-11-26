package vn.androidhaui.foxtrip.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.androidhaui.foxtrip.databinding.ItemTourBinding;
import vn.androidhaui.foxtrip.models.Tour;

public class TourAdapter extends ListAdapter<Tour, TourAdapter.VH> {

    private final Context ctx;
    private OnClickListener listener;

    public interface OnClickListener { void onClick(Tour t); }
    public void setOnClickListener(OnClickListener l) { this.listener = l; }

    public TourAdapter(Context ctx) { super(DIFF_CALLBACK); this.ctx = ctx; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTourBinding b = ItemTourBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Tour t = getItem(position);

        holder.binding.tvTourName.setText(t.getName());
        holder.binding.tvTourProvince.setText("✈\uFE0F" + t.getProvince());
        if (t.getImage() != null && !t.getImage().isEmpty())
            Glide.with(ctx).load(t.getImage().get(0)).into(holder.binding.ivTourImage);

        if (t.getDiscount() > 0) {
            double discounted = t.getPrice() * (1 - t.getDiscount() / 100.0);
            holder.binding.tvTourPrice.setText(String.format("%,.0f VND", discounted));
            holder.binding.tvTourOldPrice.setText(String.format("%,.0f VND", (double)t.getPrice()));
            holder.binding.tvTourOldPrice.setVisibility(android.view.View.VISIBLE);
            holder.binding.tvTourOldPrice.setPaintFlags(
                    holder.binding.tvTourOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            holder.binding.tvTourPrice.setText(String.format("%,.0f VND", t.getPrice()));
            holder.binding.tvTourOldPrice.setVisibility(android.view.View.GONE);
        }

        holder.binding.tvTourSlots.setText("Còn " + t.getAvailableSlots() + " chỗ");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTourBinding binding;
        VH(@NonNull ItemTourBinding b) { super(b.getRoot()); binding = b; }
    }

    private static final DiffUtil.ItemCallback<Tour> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Tour>() {
                @Override
                public boolean areItemsTheSame(@NonNull Tour oldItem, @NonNull Tour newItem) {
                    return oldItem.getSlug().equals(newItem.getSlug());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Tour oldItem, @NonNull Tour newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        android.util.Log.d("DEBUG_TOUR_ADAPTER", "Item count = " + count);
        return count;
    }

}