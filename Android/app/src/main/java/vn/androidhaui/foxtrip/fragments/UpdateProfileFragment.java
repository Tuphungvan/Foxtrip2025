package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentUpdateProfileBinding;
import vn.androidhaui.foxtrip.viewmodels.AuthViewModel;
import vn.androidhaui.foxtrip.viewmodels.UpdateProfileViewModel;

public class UpdateProfileFragment extends Fragment {

    private FragmentUpdateProfileBinding binding;
    private UpdateProfileViewModel viewModel;
    private AuthViewModel authViewModel;

    private String originalEmail = "";  // để kiểm tra khi người dùng đổi email

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUpdateProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel quản lý thông tin profile
        viewModel = new ViewModelProvider(this).get(UpdateProfileViewModel.class);

        // ViewModel xác thực dùng chung cho toàn app
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // 🟡 Tải thông tin ban đầu
        viewModel.loadProfile();
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.inputUsername.setText(user.getUsername());
                binding.inputEmail.setText(user.getEmail());
                binding.inputPhone.setText(user.getPhoneNumber());
                binding.inputAddress.setText(user.getAddress());

                // lưu email gốc để sau so sánh
                originalEmail = user.getEmail() != null ? user.getEmail() : "";
            }
        });

        // 🟡 Quan sát thông báo từ ViewModel
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        // 🟡 Bấm nút lưu
        binding.btnSave.setOnClickListener(v -> {
            String username = getText(binding.inputUsername);
            String email = getText(binding.inputEmail);
            String phone = getText(binding.inputPhone);
            String address = getText(binding.inputAddress);
            String password = getText(binding.inputPassword);

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> body = new HashMap<>();
            body.put("username", username);
            body.put("email", email);
            body.put("phoneNumber", phone);
            body.put("address", address);
            if (!TextUtils.isEmpty(password)) {
                body.put("password", password);
            }

            viewModel.updateProfile(body);
        });

        // 🟡 Khi cập nhật thành công → kiểm tra email/password → logout nếu cần
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                String newEmail = getText(binding.inputEmail);
                String newPassword = getText(binding.inputPassword);

                boolean emailChanged = !TextUtils.equals(originalEmail, newEmail);
                boolean passwordChanged = !TextUtils.isEmpty(newPassword);

                if (emailChanged || passwordChanged) {
                    Toast.makeText(requireContext(),
                            "Bạn đã thay đổi thông tin đăng nhập. Hệ thống sẽ đăng xuất...",
                            Toast.LENGTH_LONG).show();

                    // 👉 Logout dùng chung như nút đăng xuất
                    authViewModel.logout();

                } else {
                    Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });

        boolean openedFromProfile = false;
        if (getArguments() != null) {
            openedFromProfile = getArguments().getBoolean("openedFromProfile", false);
        }

        if (openedFromProfile) {

            binding.tvHeaderCustomer.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.primaryColor)
            );

            binding.tilUserName.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.primaryColor)
            );

            binding.tilEmail.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.primaryColor)
            );

            binding.tilPhone.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.primaryColor)
            );

            binding.tilAddress.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.primaryColor)
            );

            binding.tilPassword.setBoxStrokeColor(
                    ContextCompat.getColor(requireContext(), R.color.primaryColor)
            );

            binding.btnSave.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.primaryColor)
            );
        }
    }

    private String getText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
