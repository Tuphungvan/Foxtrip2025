package vn.androidhaui.foxtrip.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.androidhaui.foxtrip.databinding.ItemHistoryBinding;
import vn.androidhaui.foxtrip.models.History;

public class HistoriesAdapter extends RecyclerView.Adapter<HistoriesAdapter.VH> {
    private final List<History> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnItemClickListener { void onClick(History history); }

    public HistoriesAdapter(OnItemClickListener listener) { this.listener = listener; }

    public void setItems(List<History> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        private final ItemHistoryBinding b;
        VH(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(History h) {
            b.tvHistoryId.setText(h.get_id() != null ? "Mã số: " + h.get_id() : "—");
            String end = h.getEndDate() != null ? df.format(h.getEndDate()) : "—";
            b.tvEndDate.setText("Ngày kết thúc: " + end);

            String thumb = null;
            if (h.getItems() != null && !h.getItems().isEmpty()) {
                thumb = h.getItems().get(0).getImage();
            }
            if (thumb != null && !thumb.isEmpty()) {
                Glide.with(b.getRoot().getContext()).load(thumb).into(b.ivThumb);
            } else b.ivThumb.setImageResource(android.R.color.transparent);

            b.getRoot().setOnClickListener(v -> listener.onClick(h));
        }
    }
}
