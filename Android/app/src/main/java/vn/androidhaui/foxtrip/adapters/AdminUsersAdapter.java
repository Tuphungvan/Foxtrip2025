package vn.androidhaui.foxtrip.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Objects;
import java.util.List;

import vn.androidhaui.foxtrip.databinding.ItemAdminUserBinding;
import vn.androidhaui.foxtrip.models.User;

public class AdminUsersAdapter extends ListAdapter<User, AdminUsersAdapter.VH> {

    public interface Listener {
        void onActivate(User user);
        void onDeactivate(User user);
        void onResetPassword(User user);
    }

    private final Listener listener;

    public AdminUsersAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<User> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminUserBinding b = ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    public void reloadData(List<User> list) {
        submitList(null);
        submitList(list);
    }

    class VH extends RecyclerView.ViewHolder {
        final ItemAdminUserBinding b;

        VH(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(User u) {
            b.tvUsername.setText(u.getUsername());
            b.tvEmail.setText(u.getEmail());
            b.tvPhone.setText(u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
            b.chipAdmin.setVisibility(u.isAdmin() ? View.VISIBLE : View.GONE);
            b.chipActive.setText(u.isActive() ? "Hoạt động" : "Bị khoá");
            b.btnActivate.setText(u.isActive() ? "Khoá" : "Kích hoạt");

            Glide.with(b.imgAvatar.getContext())
                    .load(u.getAvatar() == null || u.getAvatar().isEmpty()
                            ? vn.androidhaui.foxtrip.R.drawable.ic_cart
                            : u.getAvatar())
                    .into(b.imgAvatar);

            b.getRoot().setMinimumHeight(200);

            b.btnReset.setOnClickListener(v -> listener.onResetPassword(u));
            b.btnActivate.setOnClickListener(v -> {
                if (u.isActive()) listener.onDeactivate(u);
                else listener.onActivate(u);
            });
        }
    }
}
