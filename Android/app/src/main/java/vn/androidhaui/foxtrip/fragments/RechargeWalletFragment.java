package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.DecimalFormat;

import vn.androidhaui.foxtrip.databinding.FragmentRechargeWalletBinding;
import vn.androidhaui.foxtrip.viewmodels.WalletViewModel;

public class RechargeWalletFragment extends Fragment {

    private FragmentRechargeWalletBinding binding;
    private WalletViewModel viewModel;
    private final DecimalFormat priceFormat = new DecimalFormat("#,###");
    private boolean isFormatting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRechargeWalletBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        setupPriceFormatter();

        viewModel.getBalance().observe(getViewLifecycleOwner(), amount -> {
            String formatted = priceFormat.format(amount);
            binding.tvBalance.setText("Số dư: " + formatted + " VNĐ");
        });

        // === Hiển thị thông báo từ ViewModel ===
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        viewModel.loadWallet();

        // === Xử lý nạp tiền ===
        binding.btnRecharge.setOnClickListener(v -> {
            String amountStr = binding.inputAmount.getText() != null ?
                    binding.inputAmount.getText().toString().trim() : "";
            String cleanAmount = amountStr.replaceAll("\\D", ""); // loại dấu phẩy

            if (TextUtils.isEmpty(cleanAmount)) {
                Toast.makeText(requireContext(), "Nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.recharge(cleanAmount);
        });
    }

    private void setupPriceFormatter() {
        binding.inputAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                try {
                    String cleanString = s.toString().replaceAll("\\D", "");
                    if (!cleanString.isEmpty()) {
                        long value = Long.parseLong(cleanString);
                        String formatted = priceFormat.format(value);
                        binding.inputAmount.setText(formatted);
                        binding.inputAmount.setSelection(formatted.length());
                    } else {
                        binding.inputAmount.setText("");
                    }
                } catch (Exception e) {
                    // ignore lỗi format
                }

                isFormatting = false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}