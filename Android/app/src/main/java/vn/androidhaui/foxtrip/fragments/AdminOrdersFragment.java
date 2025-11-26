package vn.androidhaui.foxtrip.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import vn.androidhaui.foxtrip.adapters.AdminOrdersAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentAdminOrdersBinding;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.viewmodels.AdminOrderViewModel;

public class AdminOrdersFragment extends Fragment {

    private FragmentAdminOrdersBinding binding;
    private AdminOrderViewModel viewModel;
    private AdminOrdersAdapter adapter;
    private String currentType = "pending";
    private boolean isSearching = false;

    private final AdminOrdersAdapter.OnActionListener actionListener = new AdminOrdersAdapter.OnActionListener() {
        @Override
        public void onDeletePending(Order order) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận Xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa đơn hàng chờ này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Chỉ gọi ViewModel khi nhấn "Xóa"
                        viewModel.deletePendingOrder(order.getId());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        @Override
        public void onConfirmPaid(Order order) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận Thanh toán")
                    .setMessage("Xác nhận chắc chắn đơn hàng đã thanh toán?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        // Chỉ gọi ViewModel khi nhấn "Xác nhận"
                        viewModel.confirmOrder(order.getId());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        @Override
        public void onConfirmExpired(Order order) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận Hoàn tất")
                    .setMessage("Xác nhận chắc chắn hoàn tất đơn hàng?")
                    .setPositiveButton("Hoàn tất", (dialog, which) -> {
                        // Chỉ gọi ViewModel khi nhấn "Hoàn tất"
                        viewModel.completeExpiredOrder(order.getId());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        @Override
        public void onItemClicked(Order order) {
            openAdminOrderDetail(order.getId());
        }
    };

    private void openAdminOrderDetail(String orderId) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, AdminOrderDetailFragment.newInstance(orderId))
                .addToBackStack(null)
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AdminOrderViewModel.class);

        setupTabs();
        setupSearchInput();
        setupRecycler();
        observeViewModel();

        // Initial load
        loadData();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.edtSearch.setText("");
            isSearching = false;
            loadData();
        });

        binding.btnScanQR.setOnClickListener(v -> openQRScanner());
    }

    private void openQRScanner() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new QRScannerFragment())
                .addToBackStack(null)
                .commit();
    }

    private void setupTabs() {
        binding.tabStatus.addTab(binding.tabStatus.newTab().setText("Chờ thanh toán"));
        binding.tabStatus.addTab(binding.tabStatus.newTab().setText("Đã thanh toán"));
        binding.tabStatus.addTab(binding.tabStatus.newTab().setText("Hoàn tất"));

        binding.tabStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                switch (pos) {
                    case 0: currentType = "pending"; break;
                    case 1: currentType = "to_confirm"; break;
                    case 2: currentType = "completed"; break;
                }

                // Chỉ load data nếu không đang search
                if (!isSearching) {
                    loadData();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * CHỈ tìm kiếm khi nhấn nút Search trên bàn phím
     */
    private void setupSearchInput() {
        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = v.getText().toString().trim();

                if (TextUtils.isEmpty(query)) {
                    // Nếu ô search rỗng, reset về load bình thường
                    isSearching = false;
                    loadData();
                } else {
                    // Thực hiện tìm kiếm
                    performSearch(query);
                }

                // Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && binding.edtSearch.getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(binding.edtSearch.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    private void setupRecycler() {
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminOrdersAdapter(requireContext(), new ArrayList<>(), currentType, actionListener);
        binding.rvOrders.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Danh sách đơn hàng (Load bình thường)
        viewModel.getOrders().observe(getViewLifecycleOwner(), orders -> {
            binding.swipeRefresh.setRefreshing(false);

            if (orders == null || orders.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.rvOrders.setVisibility(View.GONE);
                binding.tvEmpty.setText("Không có đơn hàng nào");
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                binding.rvOrders.setVisibility(View.VISIBLE);
                adapter = new AdminOrdersAdapter(requireContext(), orders, currentType, actionListener);
                binding.rvOrders.setAdapter(adapter);
            }
        });

        // Kết quả tìm kiếm
        viewModel.getSearchResult().observe(getViewLifecycleOwner(), result -> {
            binding.swipeRefresh.setRefreshing(false);

            if (result == null || result.getOrders() == null || result.getOrders().isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.rvOrders.setVisibility(View.GONE);
                binding.tvEmpty.setText("Không tìm thấy đơn hàng với mã: "
                        + binding.edtSearch.getText().toString().trim());
            } else {
                String orderStatus = result.getStatus();
                if (orderStatus != null) {
                    switchToCorrectTab(orderStatus);
                }

                binding.tvEmpty.setVisibility(View.GONE);
                binding.rvOrders.setVisibility(View.VISIBLE);

                String orderType = getTypeFromStatus(orderStatus);
                adapter = new AdminOrdersAdapter(requireContext(), result.getOrders(), orderType, actionListener);
                binding.rvOrders.setAdapter(adapter);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            if (!Boolean.TRUE.equals(loading)) {
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý kết quả hành động (xóa/xác nhận)
        viewModel.getActionResult().observe(getViewLifecycleOwner(), s -> {
            if (s != null && !s.isEmpty()) {
                Toast.makeText(requireContext(), "Thành công", Toast.LENGTH_SHORT).show();
                // Reset search và load lại
                binding.edtSearch.setText("");
                isSearching = false;
                loadData();
            }
        });
    }

    private void performSearch(String orderId) {
        isSearching = true;
        viewModel.searchOrderById(orderId);
    }

    private void loadData() {
        isSearching = false;
        viewModel.loadOrders(currentType);
    }

    private void switchToCorrectTab(String status) {
        int targetTab = -1;
        String targetType = "";

        switch (status) {
            case "Chờ thanh toán":
                targetTab = 0;
                targetType = "pending";
                break;
            case "Đã thanh toán và chờ xác nhận":
                targetTab = 1;
                targetType = "to_confirm";
                break;
            case "Hoàn tất":
                targetTab = 2;
                targetType = "completed";
                break;
        }

        if (targetTab != -1 && !currentType.equals(targetType)) {
            currentType = targetType;
            TabLayout.Tab tab = binding.tabStatus.getTabAt(targetTab);
            if (tab != null) {
                tab.select();
            }
        }
    }

    private String getTypeFromStatus(String status) {
        switch (status) {
            case "Chờ thanh toán":
                return "pending";
            case "Đã thanh toán và chờ xác nhận":
                return "to_confirm";
            case "Hoàn tất":
                return "completed";
            default:
                return "pending";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}