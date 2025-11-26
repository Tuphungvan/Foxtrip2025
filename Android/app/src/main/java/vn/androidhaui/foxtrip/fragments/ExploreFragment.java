package vn.androidhaui.foxtrip.fragments;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.androidhaui.foxtrip.MainActivity;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.adapters.TourAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentExploreBinding;
import vn.androidhaui.foxtrip.viewmodels.ExploreViewModel;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private ExploreViewModel vm;
    private TourAdapter adapter;

    // current filter state
    private final List<String> selectedProvinces = new ArrayList<>();
    private final List<String> selectedCategories = new ArrayList<>();

    private final DecimalFormat priceFormat = new DecimalFormat("#,###");
    private String selectedSort = "Mặc định";
    private Long selectedStartDate = null;
    private Long selectedEndDate = null;
    private String priceMin = null;
    private String priceMax = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ExploreViewModel.class);

        setupRecycler();
        setupSearch();
        setupFilterButton();
        setupSortSpinner();
        observeVM();

        // initial load -> no filters (will still use backend validTourCondition)
        applyFilters();
    }

    private void setupRecycler() {
        adapter = new TourAdapter(requireContext());
        binding.rvExploreTours.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvExploreTours.setNestedScrollingEnabled(false);
        binding.rvExploreTours.setAdapter(adapter);

        adapter.setOnClickListener(tour -> {
            // Open detail (reuse MainActivity's loader)
            ((MainActivity) requireActivity()).loadFragment(
                    vn.androidhaui.foxtrip.fragments.TourDetailFragment.newInstance(tour.getSlug()), false);
        });
    }

    private void setupPriceFormatter(EditText editText) {
        final boolean[] isFormatting = {false};

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting[0]) return;
                isFormatting[0] = true;

                try {
                    String cleanString = s.toString().replace(".", "");
                    if (!cleanString.isEmpty()) {
                        long value = Long.parseLong(cleanString);
                        String formatted = priceFormat.format(value);
                        editText.setText(formatted);
                        editText.setSelection(formatted.length());
                    } else {
                        editText.setText("");
                    }
                } catch (Exception e) {
                    // ignore lỗi format
                }

                isFormatting[0] = false;
            }
        });
    }

    private void setupSearch() {
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            // apply search + other filters
            applyFilters();
            hideKeyboard(v);
            return true;
        });
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void setupFilterButton() {
        binding.btnOpenFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void setupSortSpinner() {
        String[] sorts = getResources().getStringArray(R.array.sort_options);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_text,
                sorts
        ) {
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                // Ép kiểu an toàn
                TextView textView = (TextView) view;

                int selectedPos = binding.spinSort.getSelectedItemPosition();

                if (position == selectedPos) {
                    // Mục đang được chọn
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.appMainColor));
                } else {
                    // Mục chưa được chọn
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.big_text_color));
                    textView.setBackgroundColor(Color.WHITE);
                }

                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinSort.setAdapter(adapter);

        binding.spinSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSort = sorts[position];
                applyFilters();
                adapter.notifyDataSetChanged(); // cập nhật màu item đang chọn trong dropdown
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không cần xử lý
            }
        });
    }


    private void observeVM() {
        vm.getTours().observe(getViewLifecycleOwner(), tours -> {
            if (tours == null || tours.isEmpty()) {
                binding.rvExploreTours.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvExploreTours.setVisibility(View.VISIBLE);
                adapter.submitList(tours);
            }
            refreshChips();
        });

        vm.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // optionally show progress (not created). Could use swipe / progress indicator.
        });
    }

    private void applyFilters() {
        String q = binding.etSearch.getText() == null ? "" : binding.etSearch.getText().toString().trim();

        // Clean price values (remove dots for API) - THÊM DÒNG NÀY
        String cleanPriceMin = priceMin != null ? priceMin.replace(".", "") : null;
        String cleanPriceMax = priceMax != null ? priceMax.replace(".", "") : null;

        vm.applyFilters(q,
                new ArrayList<>(selectedProvinces),
                new ArrayList<>(selectedCategories),
                selectedStartDate,
                selectedEndDate,
                cleanPriceMin,
                cleanPriceMax,
                selectedSort);
    }

    private void refreshChips() {
        binding.chipGroupFilters.removeAllViews();

        // Provinces
        for (String p : selectedProvinces) {
            Chip c = createChip(p, () -> {
                selectedProvinces.remove(p);
                applyFilters();
            });
            binding.chipGroupFilters.addView(c);
        }

        // Categories
        for (String cStr : selectedCategories) {
            Chip c = createChip(cStr, () -> {
                selectedCategories.remove(cStr);
                applyFilters();
            });
            binding.chipGroupFilters.addView(c);
        }

        // Dates
        if (selectedStartDate != null || selectedEndDate != null) {
            String label;
            if (selectedStartDate != null && selectedEndDate != null) {
                label = formatDate(selectedStartDate) + " → " + formatDate(selectedEndDate);
            } else if (selectedStartDate != null) {
                label = "Bắt đầu: " + formatDate(selectedStartDate);
            } else {
                label = "Kết thúc: " + formatDate(selectedEndDate);
            }
            Chip c = createChip(label, () -> {
                selectedStartDate = null;
                selectedEndDate = null;
                applyFilters();
            });
            binding.chipGroupFilters.addView(c);
        }

        // Price
        if (!TextUtils.isEmpty(priceMin) || !TextUtils.isEmpty(priceMax)) {
            String label = (TextUtils.isEmpty(priceMin) ? "" : priceMin) + " - " + (TextUtils.isEmpty(priceMax) ? "" : priceMax);
            Chip c = createChip(label, () -> {
                priceMin = null;
                priceMax = null;
                applyFilters();
            });
            binding.chipGroupFilters.addView(c);
        }
    }

    private Chip createChip(String text, Runnable onClose) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCloseIconEnabled(true);
        chip.setOnCloseIconClickListener(v -> onClose.run());
        return chip;
    }

    private void showFilterDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);

        // find views
        EditText etPriceMin = dialogView.findViewById(R.id.etPriceMin);
        EditText etPriceMax = dialogView.findViewById(R.id.etPriceMax);
        androidx.appcompat.widget.AppCompatButton btnChooseProvince = dialogView.findViewById(R.id.btnChooseProvince);
        androidx.appcompat.widget.AppCompatButton btnChooseCategory = dialogView.findViewById(R.id.btnChooseCategory);
        androidx.appcompat.widget.AppCompatButton btnStartDate = dialogView.findViewById(R.id.btnStartDate);
        androidx.appcompat.widget.AppCompatButton btnEndDate = dialogView.findViewById(R.id.btnEndDate);
        androidx.appcompat.widget.AppCompatButton btnClear = dialogView.findViewById(R.id.btnClearFilter);
        androidx.appcompat.widget.AppCompatButton btnApply = dialogView.findViewById(R.id.btnApplyFilter);

        // populate price
        if (!TextUtils.isEmpty(priceMin)) etPriceMin.setText(priceMin);
        if (!TextUtils.isEmpty(priceMax)) etPriceMax.setText(priceMax);

        // Setup price formatters
        setupPriceFormatter(etPriceMin);
        setupPriceFormatter(etPriceMax);

        // update date buttons if dates are selected
        if (selectedStartDate != null) {
            btnStartDate.setText(formatDate(selectedStartDate));
        }
        if (selectedEndDate != null) {
            btnEndDate.setText(formatDate(selectedEndDate));
        }

        // Province multi-select dialog
        final String[] provinces = getResources().getStringArray(R.array.provinces);
        btnChooseProvince.setOnClickListener(v -> {
            boolean[] checked = new boolean[provinces.length];
            for (int i = 0; i < provinces.length; i++) {
                checked[i] = selectedProvinces.contains(provinces[i]);
            }
            AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
            b.setTitle("Chọn tỉnh/thành");
            b.setMultiChoiceItems(provinces, checked, (dialog, which, isChecked) ->
                    checked[which] = isChecked
            );
            b.setPositiveButton("OK", (dialog, which) -> {
                selectedProvinces.clear();
                AlertDialog ad = (AlertDialog) dialog;
                for (int i = 0; i < provinces.length; i++) {
                    if (ad.getListView().isItemChecked(i)) {
                        selectedProvinces.add(provinces[i]);
                    }
                }
            });
            b.setNegativeButton("Hủy", null);
            AlertDialog alertDialog = b.create();
            alertDialog.show();

            // Style the buttons
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    getResources().getColor(R.color.appMainColor, null));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    getResources().getColor(R.color.appMainColor, null));
        });

        // Category multi-select
        final String[] categories = getResources().getStringArray(R.array.categories);
        btnChooseCategory.setOnClickListener(v -> {
            boolean[] checked = new boolean[categories.length];
            for (int i = 0; i < categories.length; i++) {
                checked[i] = selectedCategories.contains(categories[i]);
            }
            AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
            b.setTitle("Chọn category");
            b.setMultiChoiceItems(categories, checked, (dialog, which, isChecked) ->
                    checked[which] = isChecked
            );
            b.setPositiveButton("OK", (dialog, which) -> {
                selectedCategories.clear();
                AlertDialog ad = (AlertDialog) dialog;
                for (int i = 0; i < categories.length; i++) {
                    if (ad.getListView().isItemChecked(i)) {
                        selectedCategories.add(categories[i]);
                    }
                }
            });
            b.setNegativeButton("Hủy", null);
            AlertDialog alertDialog = b.create();
            alertDialog.show();

            // Style the buttons
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    getResources().getColor(R.color.appMainColor, null));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    getResources().getColor(R.color.appMainColor, null));
        });

        // Date pickers
        btnStartDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày bắt đầu")
                    .setTheme(com.google.android.material.R.style.ThemeOverlay_Material3_MaterialCalendar) // Material 3
                    .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                selectedStartDate = selection;
                btnStartDate.setText(formatDate(selectedStartDate));
                btnStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.appMainColor));
            });

            picker.show(getParentFragmentManager(), "start_date");
        });



        btnEndDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày kết thúc")
                    .setTheme(com.google.android.material.R.style.ThemeOverlay_Material3_MaterialCalendar) // Material 3
                    .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                selectedEndDate = selection;
                btnEndDate.setText(formatDate(selectedEndDate));
                btnEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.appMainColor));
            });
            picker.show(getParentFragmentManager(), "end_date");
        });

        // Clear & Apply
        btnClear.setOnClickListener(v -> {
            selectedProvinces.clear();
            selectedCategories.clear();
            selectedStartDate = null;
            selectedEndDate = null;
            priceMin = null;
            priceMax = null;
            etPriceMin.setText("");
            etPriceMax.setText("");
            btnStartDate.setText("Chọn bắt đầu");
            btnEndDate.setText("Chọn kết thúc");
        });

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnApply.setOnClickListener(v -> {
            // read price
            String pm = etPriceMin.getText() == null ? null : etPriceMin.getText().toString().trim();
            String px = etPriceMax.getText() == null ? null : etPriceMax.getText().toString().trim();
            priceMin = TextUtils.isEmpty(pm) ? null : pm;
            priceMax = TextUtils.isEmpty(px) ? null : px;

            // Apply filters -> call VM
            applyFilters();
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private String formatDate(Long ms) {
        if (ms == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(ms));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}