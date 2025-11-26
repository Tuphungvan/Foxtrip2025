package vn.androidhaui.foxtrip.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.adapters.TourImageAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentTourEditBinding;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.viewmodels.AdminTourViewModel;

public class TourEditFragment extends Fragment {

    private FragmentTourEditBinding binding;
    private AdminTourViewModel viewModel;

    private TourImageAdapter imageAdapter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final DecimalFormat priceFormat = new DecimalFormat("#,###");
    private boolean isPriceFormatting = false;

    private final ActivityResultLauncher<Intent> pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    List<Uri> selectedUris = new ArrayList<>();

                    if (data.getClipData() != null) {
                        // Chọn nhiều ảnh
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            selectedUris.add(uri);
                        }
                    } else if (data.getData() != null) {
                        // Chọn 1 ảnh
                        selectedUris.add(data.getData());
                    }

                    if (!selectedUris.isEmpty()) {
                        String tourName = getText(binding.edtName);
                        if (tourName.isEmpty()) {
                            tourName = "temp_tour";
                        }
                        viewModel.addImages(selectedUris, tourName);
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTourEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(AdminTourViewModel.class);

        initSpinners();
        setupPriceFormatter();
        setupImageRecyclerView();

        // Observe ảnh
        viewModel.getImageUrls().observe(getViewLifecycleOwner(), images -> {
            imageAdapter.setImages(images);
            updateImageCounter(images);
        });

        // Observe tour hiện tại
        viewModel.getCurrentTour().observe(getViewLifecycleOwner(), tour -> {
            if (tour != null) {
                populateFields(tour);
                binding.btnSave.setText("Cập nhật");
                binding.layoutIsBookable.setVisibility(View.VISIBLE);
                binding.layoutAvailableSlots.setVisibility(View.VISIBLE);
            } else {
                clearFields();
                binding.btnSave.setText("Thêm mới");
                binding.layoutIsBookable.setVisibility(View.GONE);
                binding.layoutAvailableSlots.setVisibility(View.GONE);
            }
        });

        binding.edtStartDate.setOnClickListener(v -> showDatePicker(binding.edtStartDate));
        binding.edtEndDate.setOnClickListener(v -> showDatePicker(binding.edtEndDate));
        binding.btnAddImages.setOnClickListener(v -> openImagePicker());
        binding.btnSave.setOnClickListener(v -> onSave());

        viewModel.getMessage().observe(getViewLifecycleOwner(), m -> {
            if (m != null) {
                Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
                if (m.contains("thành công") && !m.contains("ảnh")) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);
                binding.btnAddImages.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                binding.btnAddImages.setEnabled(true);
            }
        });
    }

    private void setupImageRecyclerView() {
        imageAdapter = new TourImageAdapter(new TourImageAdapter.OnImageActionListener() {
            @Override
            public void onImageClick(String imageUrl, int position) {
                // Hiển thị ảnh full screen (tùy chọn)
                showImageFullScreen(imageUrl);
            }

            @Override
            public void onImageDelete(String imageUrl, int position) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa ảnh này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            viewModel.removeImage(imageUrl);
                            Toast.makeText(requireContext(), "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.recyclerImages.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerImages.setAdapter(imageAdapter);
    }

    private void updateImageCounter(List<String> images) {
        int count = images != null ? images.size() : 0;
        binding.tvImageCount.setText(count + " ảnh");
        binding.tvImageCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImagesLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
    }

    private void showImageFullScreen(String imageUrl) {
        // TODO: Implement full screen image viewer
        Toast.makeText(requireContext(), "Xem ảnh: " + imageUrl, Toast.LENGTH_SHORT).show();
    }

    private void initSpinners() {
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_text,
                getResources().getStringArray(R.array.provinces_array)
        );
        provinceAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinProvince.setAdapter(provinceAdapter);

        String[] regions = {"Bắc", "Trung", "Nam"};
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner_text, regions);
        regionAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinRegion.setAdapter(regionAdapter);

        String[] categories = {"Biển", "Mạo hiểm", "Thiên nhiên", "Nghỉ dưỡng", "Văn hóa"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner_text, categories);
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinCategory.setAdapter(categoryAdapter);
    }

    private void setupPriceFormatter() {
        binding.edtPrice.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isPriceFormatting) return;
                isPriceFormatting = true;

                try {
                    String cleanString = s.toString().replaceAll("\\D", "");
                    if (!cleanString.isEmpty()) {
                        long value = Long.parseLong(cleanString);
                        String formatted = priceFormat.format(value);
                        binding.edtPrice.setText(formatted);
                        binding.edtPrice.setSelection(formatted.length());
                    } else {
                        binding.edtPrice.setText("");
                    }
                } catch (Exception e) {
                    // ignore lỗi
                }

                isPriceFormatting = false;
            }
        });
    }

    private void populateFields(Tour t) {
        binding.edtName.setText(t.getName());
        binding.edtDescription.setText(t.getDescription());
        setSpinnerSelection(binding.spinProvince, t.getProvince());
        setSpinnerSelection(binding.spinRegion, t.getRegion());
        setSpinnerSelection(binding.spinCategory, t.getCategory());
        if (t.getPrice() != null) {
            binding.edtPrice.setText(priceFormat.format(t.getPrice().longValue()));
        } else {
            binding.edtPrice.setText("");
        }
        binding.edtSlots.setText(String.valueOf(t.getSlots()));
        binding.edtDiscount.setText(String.valueOf(t.getDiscount() != null ? t.getDiscount() : 0));
        binding.edtItinerary.setText(t.getItinerary());
        binding.edtVideoUrl.setText(t.getVideoId() != null ?
                "https://youtu.be/" + t.getVideoId() : "");
        binding.edtShortUrl.setText(t.getShortUrl() != null ?
                "https://youtube.com/shorts/" + t.getShortUrl() : "");
        if (t.getStartDate() != null)
            binding.edtStartDate.setText(t.getStartDate().substring(0, 10));
        if (t.getEndDate() != null)
            binding.edtEndDate.setText(t.getEndDate().substring(0, 10));
        binding.switchIsBookable.setChecked(Boolean.TRUE.equals(t.getIsBookable()));
        binding.edtAvailableSlots.setText(
                t.getAvailableSlots() != null ? String.valueOf(t.getAvailableSlots()) : String.valueOf(t.getSlots())
        );
    }

    private void clearFields() {
        binding.edtName.setText("");
        binding.edtDescription.setText("");
        binding.edtPrice.setText("");
        binding.edtSlots.setText("");
        binding.edtDiscount.setText("");
        binding.edtItinerary.setText("");
        binding.edtVideoUrl.setText("");
        binding.edtShortUrl.setText("");
        binding.edtStartDate.setText("");
        binding.edtEndDate.setText("");
        binding.switchIsBookable.setChecked(true);
        binding.edtAvailableSlots.setText("");
        binding.spinProvince.setSelection(0);
        binding.spinRegion.setSelection(0);
        binding.spinCategory.setSelection(0);
    }

    private void setSpinnerSelection(android.widget.Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker(final com.google.android.material.textfield.TextInputEditText target) {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    target.setText(sdf.format(selected.getTime()));
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void onSave() {
        String name = getText(binding.edtName);
        String desc = getText(binding.edtDescription);
        String province = binding.spinProvince.getSelectedItem().toString();
        String region = binding.spinRegion.getSelectedItem().toString();
        String category = binding.spinCategory.getSelectedItem().toString();
        String startDate = getText(binding.edtStartDate);
        String endDate = getText(binding.edtEndDate);
        String itinerary = getText(binding.edtItinerary);
        String videoUrl = getText(binding.edtVideoUrl);
        String shortUrl = getText(binding.edtShortUrl);
        String priceStr = getText(binding.edtPrice).replaceAll("\\D", "");
        String slotsStr = getText(binding.edtSlots);
        String discountStr = getText(binding.edtDiscount, "0");

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) ||
                TextUtils.isEmpty(province) || TextUtils.isEmpty(region) ||
                TextUtils.isEmpty(category) || TextUtils.isEmpty(startDate) ||
                TextUtils.isEmpty(endDate) || TextUtils.isEmpty(itinerary) ||
                TextUtils.isEmpty(videoUrl) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(slotsStr)) {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra ảnh
        List<String> images = viewModel.getImageUrls().getValue();
        if (images == null || images.isEmpty()) {
            Toast.makeText(requireContext(), "Cần ít nhất 1 ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int slots, discount;
        try {
            price = Double.parseDouble(priceStr);
            slots = Integer.parseInt(slotsStr);
            discount = Integer.parseInt(discountStr);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Sai định dạng số", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", desc);
        body.put("province", province);
        body.put("region", region);
        body.put("category", category);
        body.put("startDate", startDate);
        body.put("endDate", endDate);
        body.put("itinerary", itinerary);
        body.put("price", price);
        body.put("slots", slots);
        body.put("discount", discount);
        body.put("videoUrl", videoUrl);
        body.put("shortUrl", shortUrl.isEmpty() ? null : shortUrl);

        Tour current = viewModel.getCurrentTour().getValue();
        if (current != null) {
            body.put("isBookable", binding.switchIsBookable.isChecked());
            String available = getText(binding.edtAvailableSlots);
            int availableSlots = available.isEmpty() ? slots : Integer.parseInt(available);
            body.put("availableSlots", availableSlots);
            viewModel.updateTour(current.getId(), body);
        } else {
            body.put("isBookable", true);
            body.put("availableSlots", slots);
            viewModel.createTour(body);
        }
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText) {
        return getText(editText, "");
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText, String def) {
        return editText.getText() != null ? editText.getText().toString().trim() : def;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
