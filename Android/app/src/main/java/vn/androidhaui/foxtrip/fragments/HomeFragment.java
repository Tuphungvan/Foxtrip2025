package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.adapters.SectionAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentHomeBinding;
import vn.androidhaui.foxtrip.models.Section;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.viewmodels.HomeViewModel;
import vn.androidhaui.foxtrip.MainActivity;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel vm;
    private SectionAdapter sectionAdapter;
    private ExoPlayer player;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(HomeViewModel.class);

        // Section RecyclerView
        sectionAdapter = new SectionAdapter(requireContext());
        sectionAdapter.setOnTourClickListener(this::openTourDetail);
        binding.recyclerViewSections.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );
        binding.recyclerViewSections.setAdapter(sectionAdapter);

        // Observe sectionsLive duy nhất
        vm.getSectionsLive().observe(getViewLifecycleOwner(), sections -> {
            android.util.Log.d("DEBUG_SECTIONS_OBS", "Observed sections size: " + (sections == null ? 0 : sections.size()));
            if (sections != null && !sections.isEmpty()) sectionAdapter.submitList(sections);
        });

        setupSearch();
        setupHideKeyboard();

    }

    // Hàm mở TourDetailFragment
    private void openTourDetail(@NonNull Tour tour) {
        ((MainActivity) requireActivity())
                .loadFragment(TourDetailFragment.newInstance(tour.getSlug()), false);
    }

    private void setupSearch() {
        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String q = v.getText().toString().trim();
                if (!q.isEmpty()) {
                    vm.searchTours(q).observe(getViewLifecycleOwner(), tours -> {
                        if (tours != null && !tours.isEmpty()) {
                            // Tạo Section search
                            List<Section> searchSection = new ArrayList<>();
                            searchSection.add(new Section("Kết quả tìm kiếm", tours));
                            sectionAdapter.submitList(searchSection);
                        } else {
                            Toast.makeText(requireContext(),
                                    "Không tìm thấy tour", Toast.LENGTH_SHORT).show();
                        }
                    });
                    hideKeyboard(v);
                }
                return true;
            }
            return false;
        });
    }

    private void setupHideKeyboard() {
        // noinspection AndroidLintClickableViewAccessibility
        binding.getRoot().setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View focus = requireActivity().getCurrentFocus();
                if (focus instanceof EditText) {
                    focus.clearFocus();
                    hideKeyboard(focus);
                }
            }
            v.performClick(); // giữ accessibility
            return false;     // cho phép event tiếp tục propagate
        });
    }


    private void hideKeyboard(View v) {
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        if (player != null) {
            player.release();
            player = null;
        }
        binding = null;
        super.onDestroyView();
    }
}