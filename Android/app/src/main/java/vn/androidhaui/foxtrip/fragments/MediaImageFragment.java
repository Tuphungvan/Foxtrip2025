package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

public class MediaImageFragment extends Fragment {
    private static final String ARG_URL = "url";
    private String url;

    public static MediaImageFragment newInstance(String url) {
        MediaImageFragment fragment = new MediaImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ShapeableImageView imageView = new ShapeableImageView(requireContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ShapeableImageView.ScaleType.CENTER_CROP);

        if (getArguments() != null) url = getArguments().getString(ARG_URL);
        Glide.with(requireContext()).load(url).into(imageView);

        return imageView;
    }
}
