package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

import java.util.ArrayList;
import java.util.List;

import vn.androidhaui.foxtrip.MainActivity;
import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.databinding.FragmentVideoBinding;
import vn.androidhaui.foxtrip.databinding.ItemShortBinding;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.viewmodels.ShortViewModel;

public class VideoFragment extends Fragment {

    private FragmentVideoBinding binding;
    private ShortViewModel viewModel;
    private GestureDetector gestureDetector;

    private ItemShortBinding currentVideoBinding;
    private YouTubePlayer currentPlayer;
    private boolean isLoading = false;

    private List<Tour> videoHistory = new ArrayList<>();
    private int currentIndex = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVideoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String baseUrl = getString(R.string.base_url);
        viewModel = new ViewModelProvider(
                this,
                new ShortViewModel.Factory(requireContext(), baseUrl)
        ).get(ShortViewModel.class);

        setupGestureDetector();
        loadFirstVideo();
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {
                        loadNextVideo();
                        return true;
                    } else {
                        loadPreviousVideo();
                        return true;
                    }
                }
                return false;
            }
        });

        // 🔥 Bắt gesture ở trên & dưới
        View.OnTouchListener gestureListener = (v, event) -> gestureDetector.onTouchEvent(event);

        binding.gestureTop.setOnTouchListener(gestureListener);
        binding.gestureBottom.setOnTouchListener(gestureListener);
    }

    private void loadFirstVideo() {
        loadRandomVideo();
    }

    private void loadNextVideo() {
        if (isLoading) return;

        if (currentIndex < videoHistory.size() - 1) {
            currentIndex++;
            displayVideo(videoHistory.get(currentIndex));
        } else {
            loadRandomVideo();
        }
    }

    private void loadPreviousVideo() {
        if (isLoading) return;

        if (currentIndex > 0) {
            currentIndex--;
            displayVideo(videoHistory.get(currentIndex));
        } else {
            Toast.makeText(requireContext(), "Đây là video đầu tiên", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRandomVideo() {
        if (isLoading) return;

        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);

        viewModel.getRandomShort().observe(getViewLifecycleOwner(), tour -> {
            isLoading = false;
            binding.progressBar.setVisibility(View.GONE);

            if (tour != null) {
                if (currentIndex < videoHistory.size() - 1) {
                    videoHistory.subList(currentIndex + 1, videoHistory.size()).clear();
                }

                videoHistory.add(tour);
                currentIndex = videoHistory.size() - 1;

                if (videoHistory.size() > 50) {
                    videoHistory.remove(0);
                    currentIndex--;
                }

                displayVideo(tour);
            } else {
                Toast.makeText(requireContext(), "Không tải được video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayVideo(Tour tour) {
        releaseCurrentVideo();

        currentVideoBinding = ItemShortBinding.inflate(
                getLayoutInflater(),
                binding.videoContainer,
                false
        );

        binding.videoContainer.removeAllViews();
        binding.videoContainer.addView(currentVideoBinding.getRoot());

        getLifecycle().addObserver(currentVideoBinding.youtubePlayerView);

        String videoId = extractVideoId(tour.getShortUrl());
        currentVideoBinding.youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                currentPlayer = youTubePlayer;
                if (videoId != null) {
                    youTubePlayer.loadVideo(videoId, 0);
                }
            }
        });

        currentVideoBinding.tvTourTitle.setText(tour.getName());
        currentVideoBinding.tvTourPrice.setText(
                tour.getPrice() != null ? String.format("%,.0f VND", tour.getPrice()) : "0 VND"
        );

        // ✅ Gán sự kiện cho nút ở Fragment
        binding.btnViewDetailGlobal.setOnClickListener(v -> {
            if (tour != null) {
                TourDetailFragment fragment = TourDetailFragment.newInstance(tour.getSlug());
                ((MainActivity) requireActivity()).loadFragment(fragment, true);
            }
        });
    }

    private void releaseCurrentVideo() {
        if (currentVideoBinding != null && currentVideoBinding.youtubePlayerView != null) {
            currentVideoBinding.youtubePlayerView.release();
            getLifecycle().removeObserver(currentVideoBinding.youtubePlayerView);
            currentVideoBinding = null;
        }
        currentPlayer = null;
    }

    private String extractVideoId(String url) {
        if (url == null) return null;
        if (url.contains("shorts/")) {
            return url.substring(url.lastIndexOf("shorts/") + 7);
        }
        if (url.contains("youtu.be/")) {
            return url.substring(url.lastIndexOf("youtu.be/") + 9);
        }
        if (url.contains("watch?v=")) {
            String temp = url.substring(url.lastIndexOf("watch?v=") + 8);
            int ampersandPos = temp.indexOf('&');
            return ampersandPos != -1 ? temp.substring(0, ampersandPos) : temp;
        }
        return url;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentPlayer != null) {
            currentPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        releaseCurrentVideo();
        videoHistory.clear();
        binding = null;
        super.onDestroyView();
    }
}