package vn.androidhaui.foxtrip.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.androidhaui.foxtrip.databinding.ItemAdminTourBinding;
import vn.androidhaui.foxtrip.models.Tour;

public class AdminToursAdapter extends ListAdapter<Tour, AdminToursAdapter.VH> {

    public interface Listener {
        void onEdit(Tour tour);
        void onDelete(Tour tour);
    }

    private final Listener listener;

    public AdminToursAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Tour> DIFF = new DiffUtil.ItemCallback<Tour>() {
        @Override
        public boolean areItemsTheSame(@NonNull Tour oldItem, @NonNull Tour newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Tour oldItem, @NonNull Tour newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminTourBinding b = ItemAdminTourBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Tour t = getItem(position);
        holder.bind(t);
    }

    public void reloadData(List<Tour> newList) {
        submitList(null);
        submitList(newList);
    }

    class VH extends RecyclerView.ViewHolder {
        final ItemAdminTourBinding b;
        VH(ItemAdminTourBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Tour t) {
            b.tvName.setText(t.getName());
            b.tvPrice.setText(t.getPrice() != null ? String.format("%,.0f", t.getPrice()) + " đ" : "");
            if (t.getImage() != null && !t.getImage().isEmpty()) {
                Glide.with(b.imgThumb.getContext())
                        .load(t.getImage().get(0))
                        .placeholder(vn.androidhaui.foxtrip.R.drawable.ic_image_placeholder)
                        .into(b.imgThumb);
            } else {
                b.imgThumb.setImageResource(vn.androidhaui.foxtrip.R.drawable.ic_image_placeholder);
            }

            b.btnEdit.setOnClickListener(v -> listener.onEdit(t));
            b.btnDelete.setOnClickListener(v -> listener.onDelete(t));
        }
    }
}
