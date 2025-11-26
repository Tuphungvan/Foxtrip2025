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

import vn.androidhaui.foxtrip.MainActivity;
import vn.androidhaui.foxtrip.adapters.OrderItemsAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentOrderDetailBinding;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.viewmodels.OrderDetailViewModel;

public class OrderDetailFragment extends Fragment {
    private static final String ARG_ORDER_ID = "order_id";
    private FragmentOrderDetailBinding binding;
    private OrderDetailViewModel vm;
    private OrderItemsAdapter itemsAdapter;

    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment f = new OrderDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ORDER_ID, orderId);
        f.setArguments(b);
        return f;
    }

    public OrderDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void openTourDetail(@NonNull String slug) {
        ((MainActivity) requireActivity())
                .loadFragment(TourDetailFragment.newInstance(slug), false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(OrderDetailViewModel.class);

        itemsAdapter = new OrderItemsAdapter();
        itemsAdapter.setOnItemClickListener(item -> {
            if (item.getSlug() != null && !item.getSlug().isEmpty()) {
                openTourDetail(item.getSlug());
            }
        });

        binding.rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItems.setAdapter(itemsAdapter);

        String orderId = getArguments() != null ? getArguments().getString(ARG_ORDER_ID) : null;
        if (orderId == null) {
            binding.tvStatus.setText("Không có ID đơn hàng");
            Toast.makeText(requireContext(), "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        vm.getOrder().observe(getViewLifecycleOwner(), this::bindOrder);

        vm.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE));

        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                vm.clearMessage();
            }
        });

        vm.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                // Quay về OrdersFragment sau khi thành công
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Xử lý nút hủy đơn hàng
        binding.btnCancelOrder.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận hủy đơn hàng")
                    .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?")
                    .setPositiveButton("Hủy đơn", (dialog, which) -> vm.cancelOrder(orderId))
                    .setNegativeButton("Không", null)
                    .show();
        });

        // Xử lý nút thanh toán
        binding.btnPayOrder.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận thanh toán")
                    .setMessage("Xác nhận thanh toán đơn hàng này bằng Foxtrip Wallet?")
                    .setPositiveButton("Thanh toán", (dialog, which) -> vm.payOrder(orderId))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        vm.loadOrder(orderId);
    }

    private void bindOrder(Order o) {
        if (o == null) return;

        binding.tvOrderId.setText(o.getId() != null ? o.getId() : "—");
        binding.tvStatus.setText(o.getStatus() != null ? o.getStatus() : "—");
        binding.tvTotal.setText(String.format("%,.0f đ", o.getTotalAmount()));
        binding.tvPayment.setText(o.getPaymentMethod() != null ? o.getPaymentMethod() : "—");
        binding.tvCreatedAt.setText(o.getCreatedAt() != null ? o.getCreatedAt() : "—");

        // Hiển thị buttons nếu đơn hàng đang chờ thanh toán
        if ("Chờ thanh toán".equals(o.getStatus())) {
            binding.llActionButtons.setVisibility(View.VISIBLE);
        } else {
            binding.llActionButtons.setVisibility(View.GONE);
        }

        if (o.getCustomerInfo() != null) {
            binding.tvName.setText(o.getCustomerInfo().getUsername() != null ?
                    o.getCustomerInfo().getUsername() : "—");
            binding.tvPhone.setText(o.getCustomerInfo().getPhoneNumber() != null ?
                    o.getCustomerInfo().getPhoneNumber() : "—");
            binding.tvAddress.setText(o.getCustomerInfo().getAddress() != null ?
                    o.getCustomerInfo().getAddress() : "—");
            binding.tvEmail.setText(o.getCustomerInfo().getEmail() != null ?
                    o.getCustomerInfo().getEmail() : "—");
        } else {
            binding.tvName.setText("—");
            binding.tvPhone.setText("—");
            binding.tvAddress.setText("—");
            binding.tvEmail.setText("—");
        }

        if (o.getItems() != null && !o.getItems().isEmpty()) {
            itemsAdapter.setItems(o.getItems());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}