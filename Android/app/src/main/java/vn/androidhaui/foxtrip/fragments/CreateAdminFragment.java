package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import vn.androidhaui.foxtrip.databinding.FragmentCreateAdminBinding;
import vn.androidhaui.foxtrip.viewmodels.AdminCreateViewModel;

public class CreateAdminFragment extends Fragment {

    private FragmentCreateAdminBinding binding;
    private AdminCreateViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateAdminBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AdminCreateViewModel.class);

        setupObservers();
        setupListeners();

        return binding.getRoot();
    }

    private void setupObservers() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnCreateAdmin.setEnabled(!loading);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                clearInputs();
                Toast.makeText(requireContext(), "Tạo quản trị viên thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnCreateAdmin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirm = binding.etConfirmPassword.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
                Toast.makeText(requireContext(), "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(requireContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.createAdmin(username, email, password, phone);
        });
    }

    private void clearInputs() {
        binding.etUsername.setText("");
        binding.etEmail.setText("");
        binding.etPassword.setText("");
        binding.etConfirmPassword.setText("");
        binding.etPhone.setText("");
    }
}
