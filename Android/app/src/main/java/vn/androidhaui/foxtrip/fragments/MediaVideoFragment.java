package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import vn.androidhaui.foxtrip.R;

public class MediaVideoFragment extends Fragment {

    private static final String ARG_VIDEO_ID = "videoId";
    private String videoId;

    public static MediaVideoFragment newInstance(String videoId) {
        MediaVideoFragment fragment = new MediaVideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_ID, videoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        MaterialCardView card = (MaterialCardView) inflater.inflate(
                R.layout.item_video_card, container, false);

        YouTubePlayerView youTubePlayerView = card.findViewById(R.id.youtubePlayerView);

        getLifecycle().addObserver(youTubePlayerView);

        if (getArguments() != null) videoId = getArguments().getString(ARG_VIDEO_ID);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                if (videoId != null && !videoId.isEmpty()) {
                    youTubePlayer.cueVideo(videoId, 0);
                }
            }
        });

        return card;
    }
}
