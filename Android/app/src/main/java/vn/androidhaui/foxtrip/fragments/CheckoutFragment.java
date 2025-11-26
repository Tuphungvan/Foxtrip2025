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

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.adapters.CartAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentCheckoutBinding;
import vn.androidhaui.foxtrip.viewmodels.CheckoutViewModel;
import vn.androidhaui.foxtrip.viewmodels.WalletViewModel;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private WalletViewModel walletViewModel;
    private CheckoutViewModel viewModel;
    private CartAdapter adapter;

    public static CheckoutFragment newInstance() {
        return new CheckoutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String baseUrl = getString(vn.androidhaui.foxtrip.R.string.base_url);
        viewModel = new ViewModelProvider(this,
                new CheckoutViewModel.Factory(requireContext(), baseUrl))
                .get(CheckoutViewModel.class);

        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);

        // Quan sát số dư ví
        walletViewModel.getBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null) {
                try {
                    double value = balance;
                    binding.tvWalletBalance.setText(String.format("Số dư: %,.0f VND", value));
                } catch (NumberFormatException e) {
                    binding.tvWalletBalance.setText("Số dư: " + balance + " VND");
                }
            }
        });

        walletViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                walletViewModel.clearMessage();
            }
        });

        walletViewModel.loadWallet();

        // Setup adapter giỏ hàng
        adapter = new CartAdapter(requireContext());
        adapter.setMode(CartAdapter.MODE_READONLY);
        binding.rvCheckoutItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCheckoutItems.setAdapter(adapter);

        loadCheckout();

        // Đặt hàng (tạo nhiều orders)
        binding.btnPlaceOrder.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String address = binding.etAddress.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.endsWith("@gmail.com")) {
                Toast.makeText(requireContext(), "Email phải có định dạng @gmail.com", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.length() != 10 && phone.length() != 11) {
                Toast.makeText(requireContext(), "Số điện thoại phải có 10 hoặc 11 số", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuyển sang màn hình xác thực OTP
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,
                            OTPVerificationFragment.newInstance(email, name, phone, address))
                    .addToBackStack(null)
                    .commit();
        });

// Lắng nghe kết quả từ OTP verification
        getParentFragmentManager().setFragmentResultListener("otp_verification",
                getViewLifecycleOwner(), (requestKey, result) -> {
                    boolean verified = result.getBoolean("otp_verified", false);
                    if (verified) {
                        // OTP đã xác thực thành công -> Tiến hành đặt hàng
                        placeOrderAfterVerification();
                    }
                });

        binding.btnWalletUp.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(vn.androidhaui.foxtrip.R.id.fragment_container, new RechargeWalletFragment())
                .addToBackStack(null)
                .commit());
    }

    private void placeOrderAfterVerification() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        binding.pbLoading.setVisibility(View.VISIBLE);

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.placeOrder(name, phone, address, email).observe(getViewLifecycleOwner(), orderResponse -> {
            binding.pbLoading.setVisibility(View.GONE);
            if (orderResponse == null) {
                return;
            }

            String message;
            if ("Đã thanh toán và chờ xác nhận".equals(orderResponse.getStatus())) {
                message = String.format("Đã tạo %d đơn hàng và thanh toán thành công",
                        orderResponse.getTotalOrders());
            } else {
                message = String.format("Đã tạo %d đơn hàng, vui lòng thanh toán trong 24h",
                        orderResponse.getTotalOrders());
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new OrdersFragment())
                    .commit();
        });
    }

    private void loadCheckout() {
        binding.pbLoading.setVisibility(View.VISIBLE);
        viewModel.getCheckout().observe(getViewLifecycleOwner(), checkout -> {
            binding.pbLoading.setVisibility(View.GONE);
            if (checkout == null || checkout.getCart() == null || checkout.getCart().getItems() == null) {
                Toast.makeText(requireContext(), "Không thể tải thông tin giỏ hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.tvCheckoutItems.setText(
                    String.format("Số sản phẩm: %d", checkout.getCart().getItems().size()));
            binding.tvCheckoutTotal.setText(String.format("%,.0f VND", checkout.getTotal()));
            adapter.submitList(checkout.getCart().getItems());

            if (checkout.getUser() != null) {
                binding.etName.setText(checkout.getUser().getUsername());
                binding.etPhone.setText(checkout.getUser().getPhoneNumber());
                binding.etAddress.setText(checkout.getUser().getAddress());
                binding.etEmail.setText(checkout.getUser().getEmail());
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}