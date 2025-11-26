package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.adapters.AdminUsersAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentAdminUsersBinding;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.viewmodels.AdminUserViewModel;

import java.util.List;

public class AdminUsersFragment extends Fragment implements AdminUsersAdapter.Listener {

    private FragmentAdminUsersBinding binding;
    private AdminUserViewModel viewModel;
    private AdminUsersAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(AdminUserViewModel.class);

        adapter = new AdminUsersAdapter(this);
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerUsers.setAdapter(adapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_text, // layout hiển thị khi chưa mở dropdown
                new String[]{"Mặc định", "Tên ↑", "Tên ↓"}
        );
        sortAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinSort.setAdapter(sortAdapter);

        binding.spinSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                triggerLoad();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.edtSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerLoad();
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        viewModel.getUsers().observe(getViewLifecycleOwner(), this::renderList);
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
            binding.progressBar.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE)
        );

        triggerLoad();
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void triggerLoad() {
        String search = binding.edtSearch.getText() != null ? binding.edtSearch.getText().toString().trim() : "";
        int pos = binding.spinSort.getSelectedItemPosition();
        String sort = (pos == 1 ? "asc" : pos == 2 ? "desc" : null);
        viewModel.loadUsers(search.isEmpty() ? null : search, sort);
    }

    private void renderList(List<User> list) {
        adapter.reloadData(list);
        binding.tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivate(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận Kích hoạt")
                .setMessage("Bạn chắc chắn muốn kích hoạt tài khoản?")
                .setPositiveButton("Kích hoạt", (dialog, which) -> {
                    viewModel.activateUser(user.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDeactivate(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận khóa")
                .setMessage("Bạn chắc chắn muốn khóa tài khoản")
                .setPositiveButton("Vô hiệu hóa", (dialog, which) -> {
                    viewModel.deactivateUser(user.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onResetPassword(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận Đặt lại mật khẩu")
                .setMessage("Bạn chắc chắn muốn đặt lại mật khẩu?")
                .setPositiveButton("Đặt lại", (dialog, which) -> {
                    viewModel.resetPassword(user.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
