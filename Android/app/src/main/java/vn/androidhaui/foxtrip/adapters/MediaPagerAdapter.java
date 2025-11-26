package vn.androidhaui.foxtrip.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import vn.androidhaui.foxtrip.fragments.MediaImageFragment;
import vn.androidhaui.foxtrip.fragments.MediaVideoFragment;

public class MediaPagerAdapter extends FragmentStateAdapter {

    private final List<String> mediaList;

    public MediaPagerAdapter(@NonNull Fragment fragment, List<String> mediaList) {
        super(fragment);
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String item = mediaList.get(position);
        if (item.startsWith("video:")) {
            return MediaVideoFragment.newInstance(item.replace("video:", ""));
        } else {
            return MediaImageFragment.newInstance(item);
        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }
}
