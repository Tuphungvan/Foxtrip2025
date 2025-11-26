package vn.androidhaui.foxtrip.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vn.androidhaui.foxtrip.databinding.ItemSectionBinding;
import vn.androidhaui.foxtrip.models.Section;
import vn.androidhaui.foxtrip.models.Tour;

public class SectionAdapter extends ListAdapter<Section, SectionAdapter.VH> {

    private final Context ctx;
    private OnTourClickListener listener;

    public interface OnTourClickListener { void onClick(Tour tour); }
    public void setOnTourClickListener(OnTourClickListener l) { listener = l; }

    public SectionAdapter(Context ctx) { super(DIFF_CALLBACK); this.ctx = ctx; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSectionBinding b = ItemSectionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Section s = getItem(position);
        if (s.getTitle().equals("Tour giảm giá")) holder.binding.tvSectionTitle.setText("\uD83C\uDFF7\uFE0F\uD83D\uDCB8" + s.getTitle());
        else if (s.getTitle().equals("Tour bán chạy")) holder.binding.tvSectionTitle.setText("\uD83D\uDD25" + s.getTitle());
        else holder.binding.tvSectionTitle.setText("\uD83D\uDDFA\uFE0F" + s.getTitle());

        TourAdapter tourAdapter = new TourAdapter(ctx);
        tourAdapter.submitList(s.getTours());
        tourAdapter.setOnClickListener(t -> {
            if (listener != null) listener.onClick(t);
        });

        holder.binding.recyclerViewSection.setLayoutManager(
                new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
        holder.binding.recyclerViewSection.setAdapter(tourAdapter);
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemSectionBinding binding;
        VH(@NonNull ItemSectionBinding b) { super(b.getRoot()); binding = b; }
    }

    private static final DiffUtil.ItemCallback<Section> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Section oldItem, @NonNull Section newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Section oldItem, @NonNull Section newItem) {
                    return oldItem.getTours().equals(newItem.getTours());
                }
            };
}
