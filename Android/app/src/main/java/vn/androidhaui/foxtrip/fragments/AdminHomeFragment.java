package vn.androidhaui.foxtrip.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentAdminHomeBinding;
import vn.androidhaui.foxtrip.models.RevenueReport;
import vn.androidhaui.foxtrip.repositories.AccountRepository;
import vn.androidhaui.foxtrip.viewmodels.AccountViewModel;
import vn.androidhaui.foxtrip.viewmodels.AdminHomeViewModel;
import vn.androidhaui.foxtrip.viewmodels.AuthViewModel;

public class AdminHomeFragment extends Fragment {

    private FragmentAdminHomeBinding binding;
    private AdminHomeViewModel viewModel;
    private AuthViewModel authViewModel;
    private AccountViewModel accountViewModel;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminHomeViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        observeViewModel();
        viewModel.loadOverview();
        viewModel.loadRevenue();
        accountViewModel.loadProfile();

        // ⚡ Thêm nút logout (nếu bạn có nút trong UI)
        binding.btnLogout.setOnClickListener(v -> authViewModel.logout());

        binding.layoutUserInfo.setOnClickListener(v -> {
            UpdateProfileFragment fragment = new UpdateProfileFragment();

            // 🔹 gửi cờ đánh dấu là mở từ layoutUserInfo
            Bundle args = new Bundle();
            args.putBoolean("openedFromProfile", true);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.admin_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        initImagePicker();
        binding.imgAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void initImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Glide.with(this).load(uri).placeholder(R.drawable.ic_account).into(binding.imgAvatar);
                        uploadAvatar(uri);
                    }
                }
        );
    }

    // 📤 Upload avatar giống AccountFragment
    private void uploadAvatar(Uri uri) {
        try {
            File file = createFileFromUri(uri);

            RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), reqFile);

            AccountRepository repo = new AccountRepository(requireContext(), getString(R.string.base_url));
            repo.uploadAvatar(body, new AccountRepository.CallbackResult<>() {
                @Override
                public void onSuccess(String url) {
                    Toast.makeText(requireContext(), "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                    accountViewModel.loadProfile(); // load lại user
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(requireContext(), "Lỗi upload: " + error, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    // 📂 Convert Uri -> File tạm
    private File createFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File tempFile = new File(requireContext().getCacheDir(), "temp_avatar.jpg");
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        return tempFile;
    }

    private void observeViewModel() {
        viewModel.getOverview().observe(getViewLifecycleOwner(), this::updateOverviewUI);
        viewModel.getRevenueReports().observe(getViewLifecycleOwner(), this::updateRevenueChart);
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        authViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                authViewModel.clearMessage();
            }
        });

        accountViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            binding.tvUsername.setText(user.getUsername() != null ? user.getUsername() : "Không rõ");
            binding.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                binding.imgAvatar.setImageResource(R.drawable.ic_account);
            } else {
                Glide.with(this)
                        .load(user.getAvatar())
                        .placeholder(R.drawable.ic_account)
                        .into(binding.imgAvatar);
            }
        });
    }

    private void updateOverviewUI(Map<String, Object> data) {
        if (data == null) return;
        binding.tvTourCount.setText(String.valueOf(data.get("tourCount")));
        binding.tvUserCount.setText(String.valueOf(data.get("userCount")));
        binding.tvOrderCount.setText(String.valueOf(data.get("orderCount")));
    }

    private void updateRevenueChart(List<RevenueReport> reports) {
        if (reports == null || reports.isEmpty()) {
            binding.revenueChart.setVisibility(View.GONE);
            return;
        }

        binding.revenueChart.setVisibility(View.VISIBLE);

        // 📊 Tạo dữ liệu cột
        List<BarEntry> entries = new ArrayList<>();
        final List<String> months = new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {
            RevenueReport r = reports.get(i);
            entries.add(new BarEntry(i, (float) r.getTotalRevenue()));

            // ✅ Sửa: month là int
            months.add(formatMonth(r.getMonth()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (VNĐ)");
        dataSet.setValueTextSize(10f);
        dataSet.setColor(getResources().getColor(R.color.primaryColor, null));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatRevenue(value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        BarChart chart = binding.revenueChart;
        chart.setData(barData);

        // ✅ Cấu hình trục X (tháng)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < months.size()) {
                    return months.get(index);
                }
                return "";
            }
        });
        xAxis.setTextSize(11f);
        xAxis.setLabelRotationAngle(-45f);

        // ✅ Cấu hình trục Y bên trái
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1000000f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatRevenue(value);
            }
        });
        leftAxis.setTextSize(10f);

        // ✅ Tắt trục Y bên phải
        chart.getAxisRight().setEnabled(false);

        // ✅ Cấu hình chung
        chart.getDescription().setEnabled(false);
        chart.setFitBars(true);
        chart.setDrawValueAboveBar(true);
        chart.setExtraBottomOffset(10f);
        chart.animateY(1000);
        chart.invalidate();
    }

    // ✅ Format tháng từ int -> String
    private String formatMonth(int month) {
        return "T" + month; // T1, T2, T3...
        // Hoặc: return "Tháng " + month;
    }

    // 💰 Format doanh thu (giữ nguyên)
    private String formatRevenue(float value) {
        if (value >= 1_000_000_000) {
            return String.format("%.1ftỷ", value / 1_000_000_000);
        } else if (value >= 1_000_000) {
            return String.format("%.0ftr", value / 1_000_000);
        } else if (value >= 1000) {
            return String.format("%.0fk", value / 1000);
        }
        return String.format("%.0f", value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
