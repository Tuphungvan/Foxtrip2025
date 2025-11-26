package vn.androidhaui.foxtrip.fragments;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.androidhaui.foxtrip.MainActivity;
import vn.androidhaui.foxtrip.adapters.MediaPagerAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentTourDetailBinding;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.viewmodels.TourDetailViewModel;
import vn.androidhaui.foxtrip.R;

public class TourDetailFragment extends Fragment {

    private static final String ARG_SLUG = "slug";
    private String slug;
    private FragmentTourDetailBinding binding;
    private TourDetailViewModel viewModel;

    public static TourDetailFragment newInstance(String slug) {
        TourDetailFragment fragment = new TourDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SLUG, slug);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTourDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String baseUrl = getString(R.string.base_url);
        viewModel = new ViewModelProvider(
                this,
                new TourDetailViewModel.Factory(requireContext(), baseUrl)
        ).get(TourDetailViewModel.class);

        if (getArguments() != null) slug = getArguments().getString(ARG_SLUG);

        loadTourDetail();

        // 🟡 Xử lý tăng giảm số lượng
        final int[] quantity = {1};
        binding.tvQuantity.setText(String.valueOf(quantity[0]));

        binding.btnIncrease.setOnClickListener(v -> {
            quantity[0]++;
            binding.tvQuantity.setText(String.valueOf(quantity[0]));
        });

        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) { // không cho nhỏ hơn 1
                quantity[0]--;
                binding.tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        binding.btnAddToCart.setOnClickListener(v ->
            viewModel.addToCart(slug, quantity[0]).observe(getViewLifecycleOwner(), msg -> {
                if ("success".equals(msg)) {
                    Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            })
        );

        // 🟡 Nút back
        binding.btnBack.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack()
        );
    }


    private void loadTourDetail() {
        viewModel.loadTourDetail(slug).observe(getViewLifecycleOwner(), tour -> {
            if (tour == null) {
                Toast.makeText(requireContext(), "Không thể tải chi tiết tour", Toast.LENGTH_SHORT).show();
            } else {
                bindTourData(tour);
            }
        });
    }

    private void bindTourData(Tour tour) {
        List<String> mediaList = new ArrayList<>();
        if (tour.getImage() != null) mediaList.addAll(tour.getImage());
        if (tour.getVideoId() != null && !tour.getVideoId().isEmpty()) mediaList.add("video:" + tour.getVideoId());
        binding.mediaPager.setAdapter(new MediaPagerAdapter(this, mediaList));

        binding.tvDescription.setText(tour.getDescription() != null ? tour.getDescription() : "");

        // Tính giá sau giảm
        double price = tour.getPrice() != null ? tour.getPrice() : 0;
        int discount = tour.getDiscount() != null ? tour.getDiscount() : 0;
        double finalPrice = price * (1 - discount / 100.0);

        if (discount > 0) {
            binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            binding.tvOriginalPrice.setPaintFlags(
                    binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
            binding.tvOriginalPrice.setText(String.format("%,.0f VND", price));
        } else {
            binding.tvOriginalPrice.setVisibility(View.GONE);
        }

        binding.tvFinalPrice.setText(String.format("%,.0f VND", finalPrice));

        binding.tvTime.setText("Từ " + formatDate(tour.getStartDate()) +
                " - " + formatDate(tour.getEndDate()));

        binding.tvItinerary.setText(tour.getItinerary() != null ? tour.getItinerary().replace("\\n", "\n") : "");

        binding.tvCategory.setText(tour.getCategory() != null ? tour.getCategory() : "");
        binding.tvProvince.setText(tour.getProvince() != null ? tour.getProvince() : "");
        binding.tvRegion.setText(tour.getRegion() != null ? tour.getRegion() : "");
        binding.tvAvailableSlots.setText(tour.getAvailableSlots() != null ? tour.getAvailableSlots().toString() : "0");
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "-";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = inputFormat.parse(isoDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).findViewById(R.id.bottomNavigation).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity) requireActivity()).findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
