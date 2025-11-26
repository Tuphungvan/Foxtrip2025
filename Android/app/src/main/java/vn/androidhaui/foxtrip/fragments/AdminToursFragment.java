package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.adapters.AdminToursAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentAdminToursBinding;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.viewmodels.AdminTourViewModel;

public class AdminToursFragment extends Fragment implements AdminToursAdapter.Listener {

    private FragmentAdminToursBinding binding;
    private AdminTourViewModel viewModel;
    private AdminToursAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminToursBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(AdminTourViewModel.class);

        adapter = new AdminToursAdapter(this);
        binding.recyclerTours.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTours.setAdapter(adapter);

        // 🧠 Spinner sắp xếp
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_text, // layout hiển thị khi chưa mở dropdown
                new String[]{"Mặc định", "Tên ↑", "Tên ↓"}
        );
        sortAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinSort.setAdapter(sortAdapter);

        binding.spinSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                // Mỗi lần chọn sort thì render lại danh sách (sort client-side)
                List<Tour> currentList = viewModel.getTours().getValue();
                if (currentList != null) {
                    renderList(currentList);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.fabAdd.setOnClickListener(v -> {
            viewModel.clearCurrentTour();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.admin_fragment_container, new TourEditFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewModel.getTours().observe(getViewLifecycleOwner(), this::renderList);
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
            binding.progressBar.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE)
        );

        binding.edtSearch.setOnEditorActionListener((v1, actionId, event) -> {
            String query = v1.getText() != null ? v1.getText().toString().trim() : "";
            if (!query.isEmpty()) {
                viewModel.searchTours(query);
            } else {
                viewModel.loadTours();
            }
            hideKeyboard(v1);
            return true;
        });

        // Load tours ban đầu
        viewModel.loadTours();
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void renderList(List<Tour> list) {
        int pos = binding.spinSort.getSelectedItemPosition();

        // ✅ Collator hỗ trợ tiếng Việt (phân biệt đúng â, ă, đ,...)
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        collator.setStrength(Collator.PRIMARY); // không phân biệt hoa thường

        if (pos == 1) {
            list.sort((a, b) -> collator.compare(getFirstWord(a.getName()), getFirstWord(b.getName())));
        } else if (pos == 2) {
            list.sort((a, b) -> collator.compare(getFirstWord(b.getName()), getFirstWord(a.getName())));
        }

        adapter.reloadData(list);
    }

    private String getFirstWord(String name) {
        if (name == null || name.isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        return parts[0]; // Từ đầu tiên
    }

    @Override
    public void onEdit(Tour tour) {
        viewModel.setCurrentTour(tour);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, new TourEditFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDelete(Tour tour) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa tour")
                .setMessage("Bạn có chắc muốn xóa tour: " + tour.getName() + " ?")
                .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteTour(tour.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
