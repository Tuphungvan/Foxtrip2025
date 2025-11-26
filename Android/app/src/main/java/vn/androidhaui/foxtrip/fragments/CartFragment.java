package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.MainActivity;
import vn.androidhaui.foxtrip.adapters.CartAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentCartBinding;
import vn.androidhaui.foxtrip.models.CartItem;
import vn.androidhaui.foxtrip.viewmodels.CartViewModel;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void openTourDetail(@NonNull String slug) {
        ((MainActivity) requireActivity())
                .loadFragment(TourDetailFragment.newInstance(slug), false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String baseUrl = getString(vn.androidhaui.foxtrip.R.string.base_url);
        viewModel = new ViewModelProvider(this, new CartViewModel.Factory(requireContext(), baseUrl))
                .get(CartViewModel.class);

        adapter = new CartAdapter(requireContext());
        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCart.setAdapter(adapter);

        adapter.setCartActionListener(new CartAdapter.CartActionListener() {
            @Override
            public void onIncrease(@NonNull CartItem item) {
                viewModel.increase(item.getSlug()).observe(getViewLifecycleOwner(), msg -> {
                    if ("success".equals(msg)) {
                        loadCart(); // refresh
                    } else {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDecrease(@NonNull CartItem item) {
                viewModel.decrease(item.getSlug()).observe(getViewLifecycleOwner(), msg -> {
                    if ("success".equals(msg)) {
                        loadCart(); // refresh
                    } else {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRemove(@NonNull CartItem item) {
                viewModel.removeFromCart(item.getSlug()).observe(getViewLifecycleOwner(), msg -> {
                    if ("success".equals(msg)) {
                        loadCart();
                    } else {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onItemClick(@NonNull CartItem item) {
                openTourDetail(item.getSlug());
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadCart);

        loadCart();
        // update global cart count (if needed)
        viewModel.getCount().observe(getViewLifecycleOwner(), count -> {
            // nếu MainActivity có hàm cập nhật badge, gọi ở đây; nếu không thì bỏ qua
            try {
                ((MainActivity) requireActivity()).updateCartBadge(count);
            } catch (Exception ignored) {}
        });

        binding.btnCheckout.setOnClickListener(v -> {
            List<CartItem> currentList = adapter.getCurrentList();
            if (currentList.isEmpty()) {
                Toast.makeText(requireContext(), "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            } else {
                ((MainActivity) requireActivity())
                        .loadFragment(CheckoutFragment.newInstance(), true);
            }
        });
    }

    private void loadCart() {
        binding.pbLoading.setVisibility(View.VISIBLE);
        viewModel.loadCart().observe(getViewLifecycleOwner(), data -> {
            binding.pbLoading.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (data == null || data.getCart() == null || data.getCart().getItems() == null || data.getCart().getItems().isEmpty()) {
                adapter.submitList(new ArrayList<>());
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.tvTotal.setText(String.format("%,.0f VND", 0.0));
                return;
            }
            List<CartItem> items = data.getCart().getItems();
            adapter.submitList(items);

            double total;
            if (data.getTotal() != null) total = data.getTotal();
            else {
                total = 0;
                for (CartItem it : items) total += it.getFinalPrice() * it.getQuantity();
            }
            binding.tvTotal.setText(String.format("%,.0f VND", total));
            binding.tvEmpty.setVisibility(View.GONE);
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        try { ((MainActivity) requireActivity()).findViewById(vn.androidhaui.foxtrip.R.id.bottomNavigation).setVisibility(View.VISIBLE); } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
