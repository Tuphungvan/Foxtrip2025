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

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentOtpVerificationBinding;
import vn.androidhaui.foxtrip.viewmodels.OTPViewModel;

public class OTPVerificationFragment extends Fragment {

    private FragmentOtpVerificationBinding binding;
    private OTPViewModel viewModel;
    private String email;
    private String username;
    private String phoneNumber;
    private String address;

    public static OTPVerificationFragment newInstance(String email, String username,
                                                      String phoneNumber, String address) {
        OTPVerificationFragment fragment = new OTPVerificationFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putString("username", username);
        args.putString("phoneNumber", phoneNumber);
        args.putString("address", address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString("email");
            username = getArguments().getString("username");
            phoneNumber = getArguments().getString("phoneNumber");
            address = getArguments().getString("address");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOtpVerificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String baseUrl = getString(R.string.base_url);
        viewModel = new ViewModelProvider(this,
                new OTPViewModel.Factory(requireContext(), baseUrl))
                .get(OTPViewModel.class);

        // Quan sát message
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Gửi OTP ngay khi vào màn hình
        sendOTP();

        // Xác thực OTP
        binding.btnVerifyOTP.setOnClickListener(v -> {
            String code = binding.etOtpCode.getText().toString().trim();
            if (code.length() != 6) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.pbLoading.setVisibility(View.VISIBLE);
            viewModel.verifyOTP(email, code).observe(getViewLifecycleOwner(), success -> {
                binding.pbLoading.setVisibility(View.GONE);
                if (success != null && success) {
                    // Xác thực thành công -> Quay lại CheckoutFragment và đặt hàng
                    Bundle result = new Bundle();
                    result.putBoolean("otp_verified", true);
                    getParentFragmentManager().setFragmentResult("otp_verification", result);
                    getParentFragmentManager().popBackStack();
                }
            });
        });

        // Gửi lại OTP
        binding.btnResendOTP.setOnClickListener(v -> sendOTP());
    }

    private void sendOTP() {
        binding.pbLoading.setVisibility(View.VISIBLE);
        viewModel.sendOTP(email, username).observe(getViewLifecycleOwner(), success -> {
            binding.pbLoading.setVisibility(View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}