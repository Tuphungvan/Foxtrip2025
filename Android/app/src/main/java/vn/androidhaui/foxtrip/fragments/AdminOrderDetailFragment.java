package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.androidhaui.foxtrip.adapters.OrderItemsAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentAdminOrderDetailBinding;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.viewmodels.AdminOrderDetailViewModel;

public class AdminOrderDetailFragment extends Fragment {
    private static final String ARG_ORDER_ID = "order_id";
    private FragmentAdminOrderDetailBinding binding;
    private AdminOrderDetailViewModel vm;
    private OrderItemsAdapter itemsAdapter;
    private String currentOrderId;

    public static AdminOrderDetailFragment newInstance(String orderId) {
        AdminOrderDetailFragment f = new AdminOrderDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ORDER_ID, orderId);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(AdminOrderDetailViewModel.class);

        itemsAdapter = new OrderItemsAdapter();
        binding.rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItems.setAdapter(itemsAdapter);

        currentOrderId = getArguments() != null ? getArguments().getString(ARG_ORDER_ID) : null;
        if (currentOrderId == null) {
            binding.tvStatus.setText("Không có ID đơn hàng");
            Toast.makeText(requireContext(), "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        observeViewModel();
        setupButtons();

        vm.loadOrder(currentOrderId);
    }

    private void observeViewModel() {
        vm.getOrder().observe(getViewLifecycleOwner(), this::bindOrder);

        vm.getLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE));

        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                vm.clearMessage();
            }
        });

        vm.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                // Quay về AdminOrdersFragment sau khi thành công
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupButtons() {
        // Nút xóa đơn hàng (chờ thanh toán)
        binding.btnDeleteOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa đơn hàng này?")
                    .setPositiveButton("Xóa", (dialog, which) -> vm.deleteOrder(currentOrderId))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Nút xác nhận đơn hàng (đã thanh toán -> hoàn tất)
        binding.btnConfirmOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận đơn hàng")
                    .setMessage("Xác nhận đơn hàng này đã hoàn tất thanh toán?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> vm.confirmOrder(currentOrderId))
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Nút hoàn tất (chuyển vào lịch sử)
        binding.btnCompleteOrder.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Hoàn tất đơn hàng")
                    .setMessage("Chuyển đơn hàng này vào lịch sử và cập nhật doanh thu?")
                    .setPositiveButton("Hoàn tất", (dialog, which) -> vm.completeOrder(currentOrderId))
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void bindOrder(Order o) {
        if (o == null) return;

        binding.tvOrderId.setText(o.getId() != null ? o.getId() : "—");
        binding.tvStatus.setText(o.getStatus() != null ? o.getStatus() : "—");
        binding.tvTotal.setText(String.format("%,.0f đ", o.getTotalAmount()));
        binding.tvPayment.setText(o.getPaymentMethod() != null ? o.getPaymentMethod() : "—");
        binding.tvCreatedAt.setText(o.getCreatedAt() != null ? o.getCreatedAt() : "—");

        // Hiển thị buttons theo trạng thái
        String status = o.getStatus();
        binding.btnDeleteOrder.setVisibility(View.GONE);
        binding.btnConfirmOrder.setVisibility(View.GONE);
        binding.btnCompleteOrder.setVisibility(View.GONE);
        binding.llActionButtons.setVisibility(View.VISIBLE);

        if ("Chờ thanh toán".equals(status)) {
            binding.btnDeleteOrder.setVisibility(View.VISIBLE);
            binding.btnDeleteOrder.setText("Xóa đơn hàng");
        } else if ("Đã thanh toán và chờ xác nhận".equals(status)) {
            binding.btnConfirmOrder.setVisibility(View.VISIBLE);
            binding.btnConfirmOrder.setText("Xác nhận đơn hàng");
        } else if ("Hoàn tất".equals(status)) {
            binding.btnCompleteOrder.setVisibility(View.VISIBLE);
            binding.btnCompleteOrder.setText("Hoàn tất tour");
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